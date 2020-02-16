/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.psi.references

import com.intellij.codeInsight.controlflow.Instruction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.RecursionManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.ex.temp.TempFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.r.hints.parameterInfo.RParameterInfoUtil
import org.jetbrains.r.psi.RPsiUtil
import org.jetbrains.r.psi.api.*
import org.jetbrains.r.psi.findVariableDefinition
import org.jetbrains.r.psi.isFunctionFromLibrarySoft
import org.jetbrains.r.util.PathUtil
import java.nio.file.Path

sealed class IncludedSources {

  /**
   * @return true if all of  [element]'s possible definitions are in sourced files, false otherwise
   */
  fun resolveInSources(element: RIdentifierExpression,
                       result: MutableList<ResolveResult>,
                       localResolveResult: PsiElement?): Boolean {
    val lastConsideredSource = (localResolveResult as? RPsiElement?)?.let {
      val controlFlowHolder = PsiTreeUtil.getParentOfType(it, RControlFlowHolder::class.java)
      controlFlowHolder?.getIncludedSources(it)
    }
    val innerResult = mutableListOf<ResolveResult>()
    return resolveInSourcesInner(element, innerResult, lastConsideredSource).also {
      result.addAll(innerResult.distinct())
    }
  }

  protected abstract fun resolveInSourcesInner(element: RIdentifierExpression,
                                               result: MutableList<ResolveResult>,
                                               lastConsideredSource: IncludedSources?): Boolean

  class SingleSource(private val project: Project,
                     private val filename: String,
                     private val prev: List<IncludedSources> = emptyList()) : IncludedSources() {
    private var file: RFile? = null
      @Synchronized
      get() =
        if (field == null || !field!!.isValid) findRFile(filename, project).also { field = it }
        else field

    private fun findRFile(filename: String, project: Project): RFile? {
      val relativePath = PathUtil.toPath(filename) ?: return null
      val unitTestMode = ApplicationManager.getApplication().isUnitTestMode
      val root = if (unitTestMode) "/src" else project.basePath ?: return null
      val path = PathUtil.toPath(root)?.resolve(relativePath) ?: return null
      val virtualFile = if (unitTestMode) findTempFile(path) else VfsUtil.findFile(path, true)
      return PsiManager.getInstance(project).findFile(virtualFile ?: return null) as? RFile
    }

    override fun resolveInSourcesInner(element: RIdentifierExpression,
                                       result: MutableList<ResolveResult>,
                                       lastConsideredSource: IncludedSources?): Boolean {
      if (this == lastConsideredSource) return false
      val file = file
      file?.virtualFile?.let { virtualFile ->
        val tmpResult = mutableListOf<ResolveResult>()
        RResolver.resolveInFile(element, element.name, tmpResult, virtualFile)
        val resolveResult = tmpResult.singleOrNull()
        val resolveElement = resolveResult?.element
        val ret = RecursionManager.doPreventingRecursion(file, true) {
          if (file.getAllIncludedSources().resolveInSources(element, result, resolveElement)) return@doPreventingRecursion true
          if (resolveResult != null) {
            result.add(resolveResult)
            return@doPreventingRecursion true
          }
          return@doPreventingRecursion false
        }
        if (ret == true) return true
      }
      return prev.map { it.resolveInSourcesInner(element, result, lastConsideredSource) }.all()
    }
  }

  class MultiSource(private val multiSources: IncludedSources? = null,
                    private val prev: List<IncludedSources> = emptyList()) : IncludedSources() {
    override fun resolveInSourcesInner(element: RIdentifierExpression,
                                       result: MutableList<ResolveResult>,
                                       lastConsideredSource: IncludedSources?): Boolean {
      if (this == lastConsideredSource) return false
      return if (multiSources?.resolveInSourcesInner(element, result, lastConsideredSource) != true) {
        prev.map { it.resolveInSourcesInner(element, result, lastConsideredSource) }.all()
      }
      else true
    }
  }
}

private fun List<Boolean>.all(): Boolean {
  return if (isEmpty()) false
  else this.all { it }
}

private val EMPTY = IncludedSources.MultiSource()

private fun RControlFlowHolder.getAllIncludedSources() = includedSources.getOrDefault(controlFlow.instructions.last(), EMPTY)

fun RControlFlowHolder.analyseIncludedSources(): Map<Instruction, IncludedSources> {
  return RecursionManager.doPreventingRecursion(this, true) { analyseIncludedSourcesInner() } ?: emptyMap()
}

private fun RControlFlowHolder.analyseIncludedSourcesInner(): Map<Instruction, IncludedSources> {
  val result = mutableMapOf<Instruction, IncludedSources>()
  result[controlFlow.instructions[0]] = EMPTY
  for (instruction in controlFlow.instructions.drop(1)) {
    val prevSources = instruction.allPred()
      .filter { pred -> controlFlow.isReachable(pred) && pred.num() < instruction.num() }
      .distinct()
      .map { updateSources(result.getValue(it), it.element) }
    // Distinct to rid extra EMPTY values
    result[instruction] = prevSources.distinct().let { it.singleOrNull() ?: IncludedSources.MultiSource(prev = it) }
  }

  return result
}

private fun getSourceDeclaration(sourceIdentifier: PsiElement): RAssignmentStatement? {
  val result = mutableListOf<ResolveResult>()
  RResolver.resolveInFilesOrLibrary(sourceIdentifier, "source", result)
  if (result.isEmpty()) return null
  return result.mapNotNull { it.element }.first { RPsiUtil.isLibraryElement(it) } as RAssignmentStatement
}

private fun updateSources(sources: IncludedSources, element: PsiElement?): IncludedSources {
  if (element !is RCallExpression) return sources
  val localDefinition = (element.expression as? RIdentifierExpression)?.findVariableDefinition()?.variableDescription?.firstDefinition
  return if (localDefinition == null && element.isFunctionFromLibrarySoft("source", "base")) {
    val filepathArgument = RParameterInfoUtil.getArgumentByName(element, "file", getSourceDeclaration(element.expression))
    val filepath = if (filepathArgument is RStringLiteralExpression) filepathArgument.name else null
    if (filepath != null) IncludedSources.SingleSource(element.project, filepath, listOf(sources))
    else sources
  }
  else {
    val function = (localDefinition?.parent as? RAssignmentStatement)?.assignedValue as? RFunctionExpression
    function?.getAllIncludedSources()?.let { IncludedSources.MultiSource(it, listOf(sources)) } ?: sources
  }
}

private fun findTempFile(filePath: Path): VirtualFile? {
  val absoluteFilePath = filePath.toString()
  val fileSystem = TempFileSystem.getInstance()
  var virtualFile = fileSystem.findFileByPath(absoluteFilePath)
  if (virtualFile == null || !virtualFile.isValid) {
    virtualFile = fileSystem.refreshAndFindFileByPath(absoluteFilePath)
  }
  return virtualFile
}