/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4.context.methods

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.r.classes.s4.context.RS4ContextProvider
import org.jetbrains.r.hints.parameterInfo.RArgumentInfo
import org.jetbrains.r.psi.api.RCallExpression
import org.jetbrains.r.psi.api.RNamedArgument
import org.jetbrains.r.psi.api.RPsiElement
import org.jetbrains.r.psi.isFunctionFromLibrarySoft

sealed class RS4SetMethodContext : RS4MethodsContext() {
  override val contextFunctionName: String = "setMethod"
}

data class RS4SetMethodFunctionNameContext(override val originalElement: RPsiElement,
                                           override val contextFunctionCall: RCallExpression) : RS4SetMethodContext()

data class RS4SetMethodSignatureClassNameContext(override val originalElement: RPsiElement,
                                                 override val contextFunctionCall: RCallExpression) : RS4SetMethodContext()

// setMethod("fun", signature = c(para<caret>m = ))
data class RS4SetMethodParameterNamesContext(override val originalElement: RPsiElement,
                                             override val contextFunctionCall: RCallExpression) : RS4SetMethodContext()

data class RS4SetMethodDefinitionContext(override val originalElement: RPsiElement,
                                         override val contextFunctionCall: RCallExpression) : RS4SetMethodContext()

class RS4SetMethodProvider : RS4ContextProvider<RS4SetMethodContext>() {
  override fun getS4ContextWithoutCaching(element: RPsiElement): RS4SetMethodContext? {
    val parentCall = PsiTreeUtil.getParentOfType(element, RCallExpression::class.java) ?: return null
    return if (parentCall.isFunctionFromLibrarySoft("setMethod", "methods")) {
      val parentArgumentInfo = RArgumentInfo.getArgumentInfo(parentCall) ?: return null
      when (element) {
        parentArgumentInfo.getArgumentPassedToParameter("f") -> {
          RS4SetMethodFunctionNameContext(element, parentCall)
        }
        parentArgumentInfo.getArgumentPassedToParameter("signature") -> {
          RS4SetMethodSignatureClassNameContext(element, parentCall)
        }
        parentArgumentInfo.getArgumentPassedToParameter("definition") -> {
          RS4SetMethodDefinitionContext(element, parentCall)
        }
        else -> null
      }
    }
    else {
      val grandParentCall = PsiTreeUtil.getParentOfType(parentCall, RCallExpression::class.java) ?: return null
      if (grandParentCall.isFunctionFromLibrarySoft("setMethod", "methods")) {
        val grandParentArgumentInfo = RArgumentInfo.getArgumentInfo(grandParentCall) ?: return null
        return when {
          PsiTreeUtil.isAncestor(grandParentArgumentInfo.getArgumentPassedToParameter("signature"), element, false) -> {
            val parent = element.parent
            if (parent is RNamedArgument && parent.nameIdentifier == element) RS4SetMethodParameterNamesContext(element, grandParentCall)
            else RS4SetMethodSignatureClassNameContext(element, grandParentCall)
          }
          else -> null
        }
      }
      else null
    }
  }
}
