/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4.extra

import com.intellij.psi.PsiElement
import org.jetbrains.r.classes.s4.context.RS4ContextProvider
import org.jetbrains.r.classes.s4.context.methods.RS4SetGenericFunctionNameContext
import org.jetbrains.r.classes.s4.context.methods.RS4SetMethodFunctionNameContext
import org.jetbrains.r.classes.s4.context.setClass.RS4SetClassClassNameContext
import org.jetbrains.r.classes.s4.context.setClass.RS4SlotDeclarationContext
import org.jetbrains.r.psi.RPomTarget
import org.jetbrains.r.psi.api.RStringLiteralExpression

object RS4TargetElementEvaluator {

  fun getNamedElement(element: PsiElement): PsiElement? {
    val parent = element.parent
    if (parent is RStringLiteralExpression) {
      val context = RS4ContextProvider.getS4Context(parent,
                                                    RS4SetClassClassNameContext::class,
                                                    RS4SlotDeclarationContext::class,
                                                    RS4SetGenericFunctionNameContext::class,
                                                    RS4SetMethodFunctionNameContext::class)
      if (context != null) return RPomTarget.createStringLiteralTarget(parent)
    }
    return null
  }
}