package org.jetbrains.r.codeInsight.findUsages

import com.intellij.codeInsight.TargetElementEvaluatorEx2
import com.intellij.psi.PsiElement
import org.jetbrains.r.classes.s4.extra.RS4TargetElementEvaluator
import org.jetbrains.r.psi.api.RAssignmentStatement
import org.jetbrains.r.psi.api.RNamedArgument
import org.jetbrains.r.psi.api.RParameter

class RTargetElementEvaluator : TargetElementEvaluatorEx2() {
  override fun isAcceptableNamedParent(parent: PsiElement): Boolean {
    val grandParent = parent.parent
    if (grandParent is RAssignmentStatement) {
      return grandParent.assignee == parent
    }

    return grandParent is RParameter || grandParent is RNamedArgument
  }

  override fun getNamedElement(element: PsiElement): PsiElement? {
    return RS4TargetElementEvaluator.getNamedElement(element) ?: super.getNamedElement(element)
  }
}