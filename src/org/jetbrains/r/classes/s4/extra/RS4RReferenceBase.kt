/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4.extra

import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiElement
import org.jetbrains.r.classes.s4.RS4SourceManager
import org.jetbrains.r.classes.s4.classInfo.RS4ComplexSlotPomTarget
import org.jetbrains.r.classes.s4.classInfo.RSkeletonS4ClassPomTarget
import org.jetbrains.r.classes.s4.classInfo.RSkeletonS4SlotPomTarget
import org.jetbrains.r.classes.s4.classInfo.RStringLiteralPomTarget
import org.jetbrains.r.classes.s4.context.RS4ContextProvider
import org.jetbrains.r.classes.s4.context.methods.RS4SetGenericFunctionNameContext
import org.jetbrains.r.classes.s4.context.methods.RS4SetMethodFunctionNameContext
import org.jetbrains.r.psi.RPsiUtil
import org.jetbrains.r.psi.RSkeletonParameterPomTarget
import org.jetbrains.r.psi.api.*
import org.jetbrains.r.rinterop.RSourceFileManager

object RS4RReferenceBase {

  fun isReferenceTo(psiElement: PsiElement, element: PsiElement): Boolean {
    return element is PomTargetPsiElement &&
           element.target is RStringLiteralPomTarget &&
           psiElement is RStringLiteralExpression &&
           RS4ContextProvider.getS4Context((element.target as RStringLiteralPomTarget).literal, RS4SetGenericFunctionNameContext::class) != null &&
           RS4ContextProvider.getS4Context(psiElement, RS4SetMethodFunctionNameContext::class) != null
  }

  fun isReferenceTo(element: PsiElement, resolve: PomTargetPsiElement): Boolean {
    if (element !is RPsiElement) return false
    if (resolve.isEquivalentTo(element)) return true
    val target = resolve.target
    if (target is RStringLiteralPomTarget && element is PomTargetPsiElement) {
      val elementTarget = element.target
      if (elementTarget is RStringLiteralPomTarget &&
          RS4ContextProvider.getS4Context(elementTarget.literal, RS4SetGenericFunctionNameContext::class) != null) {
        val context = RS4ContextProvider.getS4Context(target.literal, RS4SetMethodFunctionNameContext::class)
        if (context != null && target.literal.name == elementTarget.literal.name) return true
      }
    }
    return when {
      target is RS4ComplexSlotPomTarget -> {
        val defIdentifier =
          if (target.slotDefinition is RNamedArgument) target.slotDefinition.nameIdentifier
          else target.slotDefinition
        element === defIdentifier
      }
      element.containingFile?.virtualFile != null &&
      RSourceFileManager.isTemporary(element.containingFile.virtualFile) -> {
        target is RSkeletonParameterPomTarget &&
        element is RIdentifierExpression &&
        element.parent is RParameter &&
        element.name == target.name
      }
      RS4SourceManager.isS4ClassSourceElement(element) -> {
        when (target) {
          is RSkeletonS4SlotPomTarget -> {
            return RPsiUtil.getNamedArgumentByNameIdentifier(element) != null
          }
          is RSkeletonS4ClassPomTarget -> {
            val className = (element.containingFile.firstChild as RCallExpression).argumentList.expressionList.first()
            return element == className
          }
          else -> false
        }
      }
      else -> false
    }
  }
}