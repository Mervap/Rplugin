/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.psi

import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.pom.PomNamedTarget
import com.intellij.pom.PomTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PomTargetPsiElementImpl
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.r.RLanguage
import org.jetbrains.r.classes.s4.classInfo.*
import org.jetbrains.r.classes.s4.extra.stubs.RSkeletonCallExpression
import org.jetbrains.r.console.RConsoleManager
import org.jetbrains.r.debugger.RDebuggerUtil
import org.jetbrains.r.psi.api.*
import org.jetbrains.r.rinterop.*
import org.jetbrains.r.run.visualize.VisualizeTableHandler
import org.jetbrains.r.skeleton.psi.RSkeletonAssignmentStatement

private open class RPomTargetPsiElementImpl(pomTarget: PomTarget, project: Project): PomTargetPsiElementImpl(project, pomTarget), RPsiElement {
  override fun getLanguage(): Language = RLanguage.INSTANCE
}

private class RStringLiteralPomTargetPsiElementImpl(literal: RStringLiteralExpression) :
  RPomTargetPsiElementImpl(RStringLiteralPomTarget(literal), literal.project) {
  override fun getNavigationElement() = this // hack for use RStringLiteralManipulator#getRangeInElement as target TextRange
}

abstract class RPomTarget: PomNamedTarget {
  override fun canNavigate(): Boolean = true

  override fun canNavigateToSource(): Boolean = true

  override fun isValid(): Boolean = true

  abstract fun navigateAsync(requestFocus: Boolean): Promise<Unit>

  override fun navigate(requestFocus: Boolean) {
    navigateAsync(requestFocus)
  }

  companion object {
    fun createPsiElementByRValue(rVar: RVar): RPsiElement = RPomTargetPsiElementImpl(
      createPomTarget(rVar), rVar.project)

    fun createSkeletonParameterTarget(assignment: RSkeletonAssignmentStatement, name: String): RPsiElement =
      RPomTargetPsiElementImpl(RSkeletonParameterPomTarget(assignment, name), assignment.project)

    fun createSkeletonS4SlotTarget(setClass: RSkeletonCallExpression, name: String): RPsiElement =
      RPomTargetPsiElementImpl(RSkeletonS4SlotPomTarget(setClass, name), setClass.project)

    fun createS4ComplexSlotTarget(slotDefinition: RExpression, slot: RS4ClassSlot): RPsiElement =
      RPomTargetPsiElementImpl(RS4ComplexSlotPomTarget(slotDefinition, slot), slotDefinition.project)

    fun createSkeletonS4ClassTarget(setClass: RSkeletonCallExpression): RPsiElement =
      RPomTargetPsiElementImpl(RSkeletonS4ClassPomTarget(setClass), setClass.project)

    fun createStringLiteralTarget(literal: RStringLiteralExpression): RPsiElement =
      RStringLiteralPomTargetPsiElementImpl(literal)

    fun createPomTarget(rVar: RVar): RPomTarget = when (val value = rVar.value) {
      is RValueFunction -> createFunctionPomTarget(rVar)
      is RValueDataFrame -> createDataFramePomTarget(rVar)
      is RValueGraph -> createGraphPomTarget(rVar)
      is RValueMatrix -> {
        if (value.dim.size == 2) {
          createDataFramePomTarget(rVar)
        } else {
          createVariablePomTarget(rVar)
        }
      }
      is RValueError -> throw IllegalStateException("Error: ${value.text}")
      else -> createVariablePomTarget(rVar)
    }

    fun isSkeletonPomTargetPsi(element: PsiElement): Boolean {
      if (element !is RPomTargetPsiElementImpl) return false
      val target = element.target
      return target is RSkeletonParameterPomTarget || target is RSkeletonS4ClassPomTarget || target is RSkeletonS4SlotPomTarget
    }
  }
}

private fun createDataFramePomTarget(rVar: RVar): RPomTarget = DataFramePomTarget(rVar)

private fun createGraphPomTarget(rVar: RVar): RPomTarget = GraphPomTarget(rVar)

private fun createVariablePomTarget(rVar: RVar): RPomTarget = VariablePomTarget(rVar)

private fun createFunctionPomTarget(rVar: RVar): RPomTarget = FunctionPomTarget(rVar)

internal class FunctionPomTarget(private val rVar: RVar) : RPomTarget() {
  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    return rVar.ref.rInterop.executeTask {
      rVar.ref.functionSourcePositionWithText()?.let {
        ApplicationManager.getApplication().invokeLater {
          RDebuggerUtil.navigateAndCheckSourceChanges(rVar.project, it)
        }
      }
      Unit
    }
  }

  override fun getName(): String = rVar.name
}

internal class VariablePomTarget(private val rVar: RVar) : RPomTarget() {
  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    val promise = AsyncPromise<Unit>()
    ApplicationManager.getApplication().invokeLater {
      RConsoleManager.getInstance(rVar.project).currentConsoleOrNull?.debuggerPanel?.navigate(rVar)
      promise.setResult(Unit)
    }
    return promise
  }

  override fun getName(): String = rVar.name
}

internal class DataFramePomTarget(private val rVar: RVar) : RPomTarget() {
  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    return VisualizeTableHandler.visualizeTable(rVar.ref.rInterop, rVar.ref, rVar.project, rVar.name)
  }

  override fun getName(): String = rVar.name
}

internal class GraphPomTarget(private val rVar: RVar) : RPomTarget() {
  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    return rVar.ref.evaluateAsTextAsync().then { Unit }
  }

  override fun getName(): String = rVar.name
}

internal class RSkeletonParameterPomTarget(val assignment: RSkeletonAssignmentStatement,
                                           val parameterName: String) : RPomTarget() {

  override fun navigateAsync(requestFocus: Boolean): Promise<Unit> {
    return RConsoleManager.getInstance(assignment.project).runAsync { console ->
      val rVar = assignment.createRVar(console)
      val virtualFile = rVar.ref.functionSourcePosition()?.file ?: return@runAsync
      runReadAction {
        val psiFile = PsiManager.getInstance(assignment.project).findFile(virtualFile)
        if (psiFile !is RFile) return@runReadAction
        val rFunctionExpression = PsiTreeUtil.findChildOfAnyType(psiFile, RFunctionExpression::class.java) ?: return@runReadAction
        val parameter = rFunctionExpression.parameterList?.parameterList?.firstOrNull { it.name == parameterName } ?: return@runReadAction
        invokeLater {
          parameter.navigate(true)
        }
      }
    }
  }

  override fun getName(): String = parameterName

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as RSkeletonParameterPomTarget
    if (assignment != other.assignment) return false
    if (parameterName != other.parameterName) return false
    return true
  }

  override fun hashCode(): Int {
    var result = assignment.hashCode()
    result = 31 * result + parameterName.hashCode()
    return result
  }
}