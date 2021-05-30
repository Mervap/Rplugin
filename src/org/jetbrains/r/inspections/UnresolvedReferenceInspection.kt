// Copyright (c) 2017, Holger Brandl, Ekaterina Tuzova
/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.inspections

import com.google.common.base.Joiner
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.annotations.Nls
import org.jetbrains.r.RBundle
import org.jetbrains.r.classes.s4.extra.index.RS4ClassNameIndex
import org.jetbrains.r.console.RConsoleRuntimeInfo
import org.jetbrains.r.console.runtimeInfo
import org.jetbrains.r.intentions.LoadPackageFix
import org.jetbrains.r.interpreter.RInterpreterStateManager
import org.jetbrains.r.psi.api.*
import org.jetbrains.r.psi.isFunctionFromLibrary
import org.jetbrains.r.psi.references.RReferenceBase
import org.jetbrains.r.psi.references.RSearchScopeUtil

class UnresolvedReferenceInspection : RInspection() {

  @Nls
  override fun getDisplayName(): String {
    return RBundle.message("inspection.unresolvedReference.name")
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return ReferenceVisitor(holder)
  }

  private inner class ReferenceVisitor internal constructor(private val myProblemHolder: ProblemsHolder) : RVisitor() {

    override fun visitOperator(element: ROperator) {
      if (!element.text.startsWith("%")) return
      handleResolveResult(element, element.reference)
    }

    override fun visitCallExpression(element: RCallExpression) {
      // do not try to resolve functions until we have skeletons
      if (RInterpreterStateManager.getCurrentStateOrNull(element.project)?.isSkeletonInitialized != true) return
      handleResolveResult(element.expression, element.expression.reference ?: return)
      handleS4ClassesName(element)
    }

    private fun handleResolveResult(element: RPsiElement, reference: RReferenceBase<*>) {
      val targets = reference.multiResolve(false)
      if (targets.isEmpty()) {
        myProblemHolder.registerProblem(element, UNRESOLVED_MSG, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
      }
      val runtimeInfo = element.containingFile?.runtimeInfo ?: return
      if (reference.areTargetsLoaded(false)) return
      val packageNames = targets.mapNotNull { RReferenceBase.findPackageNameByResolveResult(it) }
      registerMissingPackages(element, element.text, packageNames, runtimeInfo)
    }

    private fun handleS4ClassesName(element: RCallExpression) {
      if (!element.isFunctionFromLibrary("new", "methods")) return
      val runtimeInfo = element.containingFile?.runtimeInfo ?: return
      val loadedPackages = runtimeInfo.loadedPackages
      val classNameExpr = element.argumentList.expressionList.firstOrNull() as? RStringLiteralExpression ?: return
      val className = classNameExpr.name
      val packageNames = className?.let {
        RS4ClassNameIndex.findClassInfos(it, element.project, RSearchScopeUtil.getScope(element))
      }?.map { it.packageName }?.filter { it.isNotBlank() } ?: return
      if (packageNames.isEmpty() || packageNames.any { it in loadedPackages }) return
      registerMissingPackages(classNameExpr, className, packageNames, runtimeInfo)
    }

    private fun registerMissingPackages(element: RPsiElement, text: String, packageNames: List<String>, runtimeInfo: RConsoleRuntimeInfo) {
      val quickFixes = packageNames.map { LoadPackageFix(it, runtimeInfo) }.toTypedArray()
      val message = missingPackageMessage(text, packageNames)
      myProblemHolder.registerProblem(element, message, ProblemHighlightType.WEAK_WARNING, *quickFixes)
    }
  }

  companion object {

    var UNRESOLVED_MSG = RBundle.message("inspection.unresolvedReference.description")

    fun missingPackageMessage(symbol: String, foundIn: List<String>): String {
      return RBundle.message("inspection.unresolvedReference.missing.message", symbol, Joiner.on(", ").join(foundIn))
    }
  }
}
