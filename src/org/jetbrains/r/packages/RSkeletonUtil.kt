/*
 * Copyright 2011 Holger Brandl
 *
 * This code is licensed under BSD. For details see
 * http://www.opensource.org/licenses/bsd-license.php
 */

package org.jetbrains.r.packages

import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.r.RPluginUtil
import org.jetbrains.r.classes.s4.extra.RS4SkeletonUtil
import org.jetbrains.r.packages.LibrarySummary.RLibraryPackage
import org.jetbrains.r.packages.LibrarySummary.RLibraryPackage.*
import org.jetbrains.r.packages.LibrarySummary.RLibrarySymbol
import org.jetbrains.r.skeleton.RSkeletonFileType
import java.io.File
import java.io.IOException
import java.nio.file.Path


object RSkeletonUtil {
  private const val CUR_SKELETON_VERSION = 13
  const val SKELETON_DIR_NAME = "r_skeletons"
  private const val MAX_THREAD_POOL_SIZE = 4
  private const val FAILED_SUFFIX = ".failed"
  private const val PRIORITY_PREFIX = "## Package priority: "
  private const val DEFAULT_MAX_BUCKET_SIZE = 25 // 4 threads consume a total of ~1GB memory

  private val RAM_SIZE_HELPER by lazy { RPluginUtil.findFileInRHelpers("R/ram_size.R") }

  private val LOG = Logger.getInstance(RSkeletonUtil::class.java)

  fun checkVersion(skeletonsDirectoryPath: String) {
    val skeletonsDirectory = File(skeletonsDirectoryPath)
    val versionFile = File(skeletonsDirectory, "skeletons-version")
    if (versionFile.exists() && versionFile.readText() == CUR_SKELETON_VERSION.toString()) {
      return
    }
    skeletonsDirectory.deleteRecursively()
    if (!skeletonsDirectory.mkdirs()) {
      if (!skeletonsDirectory.exists())
        throw IOException("Can't create skeletons directory")
    }

    versionFile.printWriter().use {
      it.print(CUR_SKELETON_VERSION)
    }
  }

  private fun hash(libraryPath: String): String {
    return Path.of(libraryPath).joinToString(separator = "", postfix = "-${libraryPath.hashCode()}") { it.toString().subSequence(0, 1) }
  }

  fun installedPackageToSkeletonPath(skeletonsDirectory: String, installedPackage: RInstalledPackage): Path {
    val dirName = installedPackage.name + "-" + installedPackage.version
    return Path.of(skeletonsDirectory, dirName, hash(installedPackage.canonicalPackagePath) + "." + RSkeletonFileType.EXTENSION)
  }

  fun installedPackageToSkeletonFile(skeletonsDirectory: String, installedPackage: RInstalledPackage): VirtualFile? {
    val skeletonPath = installedPackageToSkeletonPath(skeletonsDirectory, installedPackage)
    return VfsUtil.findFile(skeletonPath, false)
  }

  fun skeletonFileToRPackage(skeletonFile: PsiFile): RPackage? = RPackage.getOrCreateRPackageBySkeletonFile(skeletonFile)

  fun skeletonFileToRPackage(skeletonFile: VirtualFile): RPackage? {
    val (name, version) = skeletonFile.parent.name.split('-', limit = 2)
                            .takeIf { it.size == 2 }
                            ?.let { Pair(it[0], it[1]) } ?: return null
    return RPackage(name, version)
  }

  fun getPriorityFromSkeletonFile(file: File): RPackagePriority? {
    return try {
      val priority = file.inputStream().use {
        parseFrom(it).priority
      }
      return when (priority) {
        Priority.NA -> RPackagePriority.NA
        Priority.BASE -> RPackagePriority.BASE
        Priority.RECOMMENDED -> RPackagePriority.RECOMMENDED
        else -> null
      }
    } catch (e: Exception) {
      LOG.warn("Failed to read package priority from skeleton file $file", e)
      null
    }
  }

  fun isBanned(packageName: String) =
    packageName == "tcltk" && SystemInfo.isMac ||
    packageName == "translations"

  private const val invalidPackageFormat = "Invalid package summary format"

  fun convertToBinFormat(packageName: String, packageSummary: String): RLibraryPackage {
    val packageBuilder = newBuilder().setName(packageName)
    packageBuilder.setName(packageName)
    val lines: List<String> = packageSummary.lines()
    if (lines.isEmpty()) throw IOException("Empty summary")

    val priority = when (val it = lines[0].trim()) {
      "", "NA" -> Priority.NA
      "BASE" -> Priority.BASE
      "RECOMMENDED" -> Priority.RECOMMENDED
      "OPTIONAL" -> Priority.OPTIONAL
      else -> {
        LOG.error("Unknown priority for package $packageName: $it",
                  Attachment("$packageName.RSummary", packageSummary))
        Priority.NA
      }
    }
    packageBuilder.setPriority(priority)

    val symbols = lines.subList(1, lines.size)

    var index = 0
    while (index < symbols.size) {
      val lineNum = index + 1
      val line = symbols[index]
      index++

      val parts = line.split('\u0001')

      if (parts.size < 3) {
        throw IOException("Too short line $lineNum: " + line)
      }

      val methodName = parts[0]
      val exported = parts[1] == "TRUE"
      val builder = RLibrarySymbol.newBuilder()
        .setName(methodName)
        .setExported(exported)

      val (types, typesEndIndex) = RS4SkeletonUtil.readRepeatedAttribute(parts, 2) {
        throw IOException("Expected $it types in line $lineNum: $line")
      }

      if (types.contains("function")) {
        if (typesEndIndex < parts.size) {
          //No "function" description for exported symbols like `something <- .Primitive("some_primitive")`

          val functionRepresentationBuilder = RLibrarySymbol.FunctionRepresentation.newBuilder()
          val functionSignatureStartIndex = RS4SkeletonUtil.convertFunctionToBinFormat(builder, parts, types, typesEndIndex, line, lineNum)
          val signature = parts[functionSignatureStartIndex]
          val prefix = "function ("
          if (!signature.startsWith(prefix) || !signature.endsWith(") ")) {
            throw IOException("Invalid function description at $lineNum: " + signature)
          }

          val parameters = signature.substring(prefix.length, signature.length - 2)
          functionRepresentationBuilder.parameters = parameters

          if (builder.type == RLibrarySymbol.Type.FUNCTION && parts.size > functionSignatureStartIndex + 1) {
            val extraNamedArgsBuilder = RLibrarySymbol.FunctionRepresentation.ExtraNamedArguments.newBuilder()
            extraNamedArgsBuilder.addAllArgNames(parts[functionSignatureStartIndex + 1].split(";"))
            extraNamedArgsBuilder.addAllFunArgNames(parts[functionSignatureStartIndex + 2].split(";"))
            functionRepresentationBuilder.extraNamedArguments = extraNamedArgsBuilder.build()
          }
          builder.setFunctionRepresentation(functionRepresentationBuilder)
        }
        else {
          builder.type = RLibrarySymbol.Type.PRIMITIVE
        }
      }
      else if (types.contains("classRepresentation") && parts.size > typesEndIndex) {
        RS4SkeletonUtil.convertS4ClassToBinFormat(builder, packageName, parts, typesEndIndex, line, lineNum)
      }
      else if (types.contains("data.frame")) {
        builder.type = RLibrarySymbol.Type.DATASET
      }
      packageBuilder.addSymbols(builder.build())
    }
    return packageBuilder.build()
  }
}

data class RPackage(val name: String, val version: String) {
  companion object {
    /**
     * if [file] type is Skeleton File Type, returns package and version which was used for its generation or null otherwise
     */
    fun getOrCreateRPackageBySkeletonFile(file: PsiFile): RPackage? {
      if (file.virtualFile.fileType != RSkeletonFileType) return null
      return CachedValuesManager.getCachedValue(file) {
        CachedValueProvider.Result(RSkeletonUtil.skeletonFileToRPackage(file.virtualFile), file)
      }
    }
  }
}