/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4.extra

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.icons.AllIcons
import org.jetbrains.r.classes.s4.methods.RS4MethodsUtil.associatedS4GenericInfo
import org.jetbrains.r.editor.completion.*
import org.jetbrains.r.hints.parameterInfo.RArgumentInfo

import org.jetbrains.r.packages.RPackage
import org.jetbrains.r.psi.api.*
import javax.swing.Icon

object RS4LookupElementFactory {

  fun createS4GenericLookupElement(functionInsertHandler: RLookupElementInsertHandler,
                                   genericExpression: RS4GenericOrMethodHolder): LookupElement {
    val functionParameters =
      when (genericExpression) {
        is RAssignmentStatement -> genericExpression.functionParameters
        is RCallExpression -> {
          when (val def = RArgumentInfo
.getArgumentByName(genericExpression, "def")) {
            is RFunctionExpression -> def.parameterList?.text
            is RIdentifierExpression -> (def.reference.resolve() as? RAssignmentStatement)?.functionParameters
            else -> null
          }
        }
        else -> null
      } ?: ""
    val name = genericExpression.associatedS4GenericInfo!!.methodName
    return functionInsertHandler.createFunctionLookupElement(name, AllIcons.Nodes.Method,
                                                             functionParameters,
                                                             genericExpression,
                                                             genericExpression is RCallExpression)
  }

  fun RLookupElementInsertHandler.createFunctionLookupElement(name: String,
                                                                      icon: Icon,
                                                                      functionParameters: String,
                                                                      def: RPsiElement,
                                                                      isLocal: Boolean = false): LookupElement {
    val packageName = if (isLocal) null else RPackage.getOrCreateRPackageBySkeletonFile(def.containingFile)?.name
    return RLookupElementFactory.createLookupElementWithGrouping(RLookupElement(name, false, icon, packageName, functionParameters),
                                                                 getInsertHandlerForFunctionCall(functionParameters),
                                                                 if (isLocal) VARIABLE_GROUPING else GLOBAL_GROUPING)
  }
}