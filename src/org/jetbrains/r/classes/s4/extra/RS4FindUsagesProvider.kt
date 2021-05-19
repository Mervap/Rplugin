// Copyright (c) 2017, Holger Brandl, Ekaterina Tuzova
/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4.extra

import com.intellij.psi.PsiElement
import org.jetbrains.r.RBundle
import org.jetbrains.r.classes.s4.context.RS4ContextProvider
import org.jetbrains.r.classes.s4.context.setClass.RS4SetClassClassNameContext
import org.jetbrains.r.classes.s4.context.setClass.RS4SlotDeclarationContext
import org.jetbrains.r.packages.LibrarySummary
import org.jetbrains.r.psi.RPsiUtil
import org.jetbrains.r.psi.api.RPsiElement
import org.jetbrains.r.psi.api.RStringLiteralExpression
import org.jetbrains.r.skeleton.psi.RSkeletonAssignmentStatement

object RS4FindUsagesProvider {

  fun getType(element: PsiElement): String? {
    if (element is RSkeletonAssignmentStatement) {
      return when (element.stub.type) {
        LibrarySummary.RLibrarySymbol.Type.S4GENERIC -> RBundle.message("find.usages.s4.generic")
        LibrarySummary.RLibrarySymbol.Type.S4METHOD -> RBundle.message("find.usages.s4.method")
        else -> null
      }
    }

    if (element is RStringLiteralExpression &&
        RS4ContextProvider.getS4Context(element, RS4SetClassClassNameContext::class) != null) {
      return RBundle.message("find.usages.s4.class")
    }

    if (RPsiUtil.getNamedArgumentByNameIdentifier(element as RPsiElement) != null &&
        RS4ContextProvider.getS4Context(element, RS4SlotDeclarationContext::class) != null) {
      return RBundle.message("find.usages.s4.slot")
    }

    return null
  }
}
