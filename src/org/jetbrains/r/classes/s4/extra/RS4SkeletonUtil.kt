/*
 * Copyright 2011 Holger Brandl
 *
 * This code is licensed under BSD. For details see
 * http://www.opensource.org/licenses/bsd-license.php
 */

package org.jetbrains.r.classes.s4.extra

import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressIndicatorProvider
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.r.RPluginUtil
import org.jetbrains.r.interpreter.RMultiOutputProcessor
import org.jetbrains.r.interpreter.runHelper
import org.jetbrains.r.interpreter.runMultiOutputHelper
import org.jetbrains.r.interpreter.uploadFileToHost
import org.jetbrains.r.packages.LibrarySummary.RLibraryPackage
import org.jetbrains.r.packages.LibrarySummary.RLibrarySymbol
import org.jetbrains.r.packages.RPackage
import org.jetbrains.r.packages.RSkeletonUtil
import org.jetbrains.r.packages.RSkeletonUtil.installedPackageToSkeletonPath
import org.jetbrains.r.packages.RSkeletonUtil.isBanned
import org.jetbrains.r.packages.remote.RepoUtils
import org.jetbrains.r.rinterop.RInterop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Integer.min
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


object RS4SkeletonUtil {
  private const val CUR_SKELETON_VERSION = 12
  private const val MAX_THREAD_POOL_SIZE = 4
  private const val DEFAULT_MAX_BUCKET_SIZE = 25 // 4 threads consume a total of ~1GB memory

  private val RAM_SIZE_HELPER by lazy { RPluginUtil.findFileInRHelpers("R/ram_size.R") }

  private val LOG = Logger.getInstance(RSkeletonUtil::class.java)

  fun updateSkeletons(interop: RInterop, progressIndicator: ProgressIndicator? = null): Boolean {
    val state = interop.state
    val generationList = mutableListOf<Pair<RPackage, Path>>()
    val installedPackages = state.installedPackages

    for (installedPackage in installedPackages) {
      val skeletonPath = installedPackageToSkeletonPath(state.skeletonsDirectory, installedPackage)
      val skeletonFile = skeletonPath.toFile()
      if (!skeletonFile.exists() && !isBanned(installedPackage.name)) {
        val rPackage = RPackage(installedPackage.name, installedPackage.version)
        Files.createDirectories(skeletonPath.parent)
        generationList.add(rPackage to skeletonPath)
      }
    }
    return generateSkeletons(generationList, interop, progressIndicator)
  }

  internal fun generateSkeletons(generationList: List<Pair<RPackage, Path>>,
                                 interop: RInterop,
                                 progressIndicator: ProgressIndicator? = null): Boolean {
    if (generationList.isEmpty()) return false
    var result = false

    val es = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE)

    val promises = mutableListOf<AsyncPromise<Boolean>>()
    val fullSize = generationList.size
    val newPackages = generationList.shuffled() // to increase the probability of uniform distribution between threads
    var bucketSize = newPackages.size / MAX_THREAD_POOL_SIZE
    if (newPackages.size % MAX_THREAD_POOL_SIZE != 0) ++bucketSize
    val maxBucketSize = getLimitedByRAMBucketSize(interop)
    bucketSize = min(bucketSize, maxBucketSize)
    for (i in newPackages.indices step bucketSize) {
      val rPackages = (i until i + bucketSize).mapNotNull { newPackages.getOrNull(it) }
      val skeletonFiles = rPackages.map { it.second.toFile() }
      val indicator: ProgressIndicator? = progressIndicator ?: ProgressIndicatorProvider.getInstance().progressIndicator
      val skeletonProcessor = RSkeletonProcessor(es, interop, indicator, fullSize, rPackages.map { it.first }, skeletonFiles)
      promises.add(skeletonProcessor.runSkeletonHelper())
    }

    for (promise in promises) {
      val timeout = maxBucketSize * 15 * 1000 // 15 seconds for each package
      promise.blockingGet(timeout)?.let { result = result || it } // Wait for all helpers
    }

    try {
      es.shutdown()
      es.awaitTermination(1, TimeUnit.HOURS)
    }
    catch (e: InterruptedException) {
      e.printStackTrace()
    }
    return result
  }

  private fun getLimitedByRAMBucketSize(interop: RInterop): Int {
    val ram = interop.interpreter.runHelper(RAM_SIZE_HELPER, emptyList()).toDoubleOrNull()
    return if (ram == null) DEFAULT_MAX_BUCKET_SIZE
    else when {
      // See more information in testData/misc/skeleton_ram_usage.csv
      ram >= 16 -> 100 // ~2.5 GB
      ram >= 2 -> 25   // ~1 GB
      ram >= 1 -> 5    // ~0.5 GB
      else -> 1
    }
  }

  fun convertFunctionToBinFormat(builder: RLibrarySymbol.Builder,
                                 parts: List<String>,
                                 types: List<String>,
                                 typesEndIndex: Int,
                                 line: String,
                                 lineNum: Int): Int {
    val functionRepresentationBuilder = RLibrarySymbol.FunctionRepresentation.newBuilder()
    return when {
      types.contains("standardGeneric") -> {
        builder.type = RLibrarySymbol.Type.S4GENERIC
        val (argNames, argNamesEndIndex) = readRepeatedAttribute(parts, typesEndIndex) {
          throw IOException("Expected $it argument names in line $lineNum: $line")
        }
        val (valueClasses, valueClassesEndIndex) = readRepeatedAttribute(parts, argNamesEndIndex) {
          throw IOException("Expected $it value classes in line $lineNum: $line")
        }
        val s4GenericSignature = RLibrarySymbol.FunctionRepresentation.S4GenericSignature.newBuilder()
          .addAllParameters(argNames)
          .addAllValueClasses(valueClasses)
          .build()
        functionRepresentationBuilder.s4GenericSignature = s4GenericSignature
        valueClassesEndIndex
      }
      types.contains("MethodDefinition") -> {
        builder.type = RLibrarySymbol.Type.S4METHOD
        val (argNames, argNamesEndIndex) = readRepeatedAttribute(parts, typesEndIndex) {
          throw IOException("Expected $it argument names in line $lineNum: $line")
        }
        val (argTypes, argTypesEndIndex) = readRepeatedAttribute(parts, argNamesEndIndex) {
          throw IOException("Expected $it argument types in line $lineNum: $line")
        }
        val s4MethodTypes = (argNames zip argTypes).map { (name, type) ->
          RLibrarySymbol.FunctionRepresentation.S4MethodParameter.newBuilder().setName(name).setType(type).build()
        }
        val parametersWrapper = RLibrarySymbol.FunctionRepresentation.S4MethodParametersWrapper.newBuilder()
        parametersWrapper.addAllS4MethodParameters(s4MethodTypes)
        functionRepresentationBuilder.s4ParametersInfo = parametersWrapper.build()
        argTypesEndIndex
      }
      else -> {
        builder.type = RLibrarySymbol.Type.FUNCTION
        typesEndIndex
      }
    }
  }

  fun convertS4ClassToBinFormat(builder: RLibrarySymbol.Builder,
                                packageName: String,
                                parts: List<String>,
                                typesEndIndex: Int,
                                line: String,
                                lineNum: Int) {
    builder.type = RLibrarySymbol.Type.S4CLASS
    val s4ClassRepresentationBuilder = RLibrarySymbol.S4ClassRepresentation.newBuilder()

    s4ClassRepresentationBuilder.packageName = packageName
    val (slots, slotsEndIndex) = readRepeatedAttribute(parts, typesEndIndex) {
      throw IOException("Expected $it slots in line $lineNum: $line")
    }
    val (superClasses, superClassesEndIndex) = readRepeatedAttribute(parts, slotsEndIndex) {
      throw IOException("Expected $it superClasses in line $lineNum: $line")
    }
    val isVirtual = parts[superClassesEndIndex] == "TRUE"

    for (i in slots.indices step 3) {
      val slotBuilder = RLibrarySymbol.S4ClassRepresentation.S4ClassSlot.newBuilder()
      slotBuilder.name = slots[i]
      slotBuilder.type = slots[i + 1]
      slotBuilder.declarationClass = slots[i + 2]
      s4ClassRepresentationBuilder.addSlots(slotBuilder)
    }

    for (i in superClasses.indices step 2) {
      val superClassBuilder = RLibrarySymbol.S4ClassRepresentation.S4SuperClass.newBuilder()
      superClassBuilder.name = superClasses[i]
      superClassBuilder.distance = superClasses[i + 1].toInt()
      s4ClassRepresentationBuilder.addSuperClasses(superClassBuilder)
    }

    s4ClassRepresentationBuilder.isVirtual = isVirtual
    builder.setS4ClassRepresentation(s4ClassRepresentationBuilder)
  }

  inline fun readRepeatedAttribute(parts: List<String>, ind: Int, onError: (Int) -> Unit): Pair<List<String>, Int> {
    val attrNumber = parts[ind].toInt()
    val attrEndIndex = ind + attrNumber + 1
    if (parts.size < attrEndIndex) {
      onError(attrNumber)
      return emptyList<String>() to -1
    }
    return parts.subList(ind + 1, attrEndIndex) to attrEndIndex
  }

  class RSkeletonProcessor(private val es: ExecutorService,
                                   rInterop: RInterop,
                                   private val indicator: ProgressIndicator?,
                                   private val allNewPackagesCnt: Int,
                                   private val rPackages: List<RPackage>,
                                   private val skeletonFiles: List<File>) : RMultiOutputProcessor {

    private val resPromise = AsyncPromise<Boolean>()
    private var curPackage: Int = -1
    private val rInterpreter = rInterop.interpreter
    private val workingDir = rInterop.workingDir.takeIf { it.isNotEmpty() } ?: rInterpreter.basePath
    private val extraNamedArgumentsHelperPath = rInterpreter.uploadFileToHost(extraNamedArgumentsHelper)
    private val packageNames = rPackages.map { it.name }
    private var hasGeneratedSkeletons = false

    override fun beforeStart() {
      indicator?.isIndeterminate = false
      nextPackageProcess()
    }

    override fun onOutputAvailable(output: String) {
      val rPackage = rPackages[curPackage]
      val skeletonFile = skeletonFiles[curPackage]
      nextPackageProcess()
      try {
        if (output.startsWith("intellij-cannot-load-package")) {
          LOG.warn("Cannot load package $rPackage in R interpreter")
          return
        }
        val binPackage: RLibraryPackage = RSkeletonUtil.convertToBinFormat(rPackage.name, output)
        FileOutputStream(skeletonFile).use { binPackage.writeTo(it) }
        hasGeneratedSkeletons = true
      }
      catch (e: Throwable) {
        val attachments = arrayOf(Attachment("$rPackage.RSummary", output))
        LOG.error("Failed to generate skeleton for '$rPackage'. The reason was:", e, *attachments)
      }
    }

    override fun onTerminated(exitCode: Int, stderr: String) {
      if (exitCode != 0) {
        val errorSuffix = """
          The error was:
          
          $stderr
          
          If you think this issue with plugin and not your R installation, please file a ticket
        """.trimIndent()
        if (curPackage < rPackages.size) {
          LOG.error("Failed to generate skeleton for '" + rPackages[curPackage] + "'. $errorSuffix")
        } else {
          LOG.error("Skeleton generation has not zero exit code. $errorSuffix")
        }
        if (curPackage < rPackages.size - 1) {
          runSkeletonHelper() // Rerun helper for tail
          return
        }
      }
      resPromise.setResult(hasGeneratedSkeletons)
    }

    /**
     * @return an [AsyncPromise] that will be set when generation is complete for all [rPackages].
     * Set to `true` if at least 1 skeleton is generated successfully. `False` otherwise
     */
    fun runSkeletonHelper(): AsyncPromise<Boolean> {
      val resPromise = resPromise
      es.submit {
        val packageNames = packageNames.subList(curPackage + 1, packageNames.size)
        rInterpreter.runMultiOutputHelper(RepoUtils.PACKAGE_SUMMARY, workingDir,
                                          listOf(extraNamedArgumentsHelperPath) + packageNames, this)
      }
      return resPromise
    }

    private fun nextPackageProcess() {
      ++curPackage
      if (curPackage < rPackages.size) {
        indicator?.apply {
          fraction += 1.0 / allNewPackagesCnt
          text = "Generating bin for '${rPackages[curPackage]}'"
        }
      }
    }

    companion object {
      private val extraNamedArgumentsHelper = RPluginUtil.findFileInRHelpers("R/extraNamedArguments.R")
    }
  }
}

val RLibrarySymbol.Type.isFunctionDeclaration
  get() = this == RLibrarySymbol.Type.FUNCTION || this == RLibrarySymbol.Type.S4GENERIC || this == RLibrarySymbol.Type.S4METHOD