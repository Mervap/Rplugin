/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4.extra

import org.jetbrains.r.classes.s4.classInfo.RS4ClassInfo
import org.jetbrains.r.classes.s4.classInfo.RS4ClassSlot
import org.jetbrains.r.classes.s4.classInfo.RS4SuperClass
import org.jetbrains.r.classes.s4.extra.stubs.RSkeletonCallExpressionStub
import org.jetbrains.r.classes.s4.methods.*
import org.jetbrains.r.packages.LibrarySummary
import org.jetbrains.r.skeleton.psi.RSkeletonElementTypes.R_SKELETON_CALL_EXPRESSION
import org.jetbrains.r.skeleton.psi.RSkeletonFileStub

object RS4SkeletonFileStubBuilder {

  fun buildStubForS4Class(skeletonFileStub: RSkeletonFileStub, symbol: LibrarySummary.RLibrarySymbol): RSkeletonCallExpressionStub {
    val s4ClassRepresentation = symbol.s4ClassRepresentation
    return RSkeletonCallExpressionStub(skeletonFileStub,
                                       R_SKELETON_CALL_EXPRESSION,
                                       RS4ClassInfo(symbol.name,
                                                    s4ClassRepresentation.packageName,
                                                    s4ClassRepresentation.slotsList.map {
                                                      RS4ClassSlot(it.name, it.type, it.declarationClass)
                                                    },
                                                    s4ClassRepresentation.superClassesList.map {
                                                      RS4SuperClass(it.name, it.distance)
                                                    },
                                                    s4ClassRepresentation.isVirtual))
  }

  fun getGenericInfoOrExtraArgs(symbol: LibrarySummary.RLibrarySymbol): Pair<RS4GenericOrMethodInfo?, LibrarySummary.RLibrarySymbol.FunctionRepresentation.ExtraNamedArguments> {
    val functionRepresentation = symbol.functionRepresentation
    return when (symbol.type) {
      LibrarySummary.RLibrarySymbol.Type.S4GENERIC -> {
        val signature = functionRepresentation.s4GenericSignature.let { RS4GenericSignature(it.parametersList, it.valueClassesList, false) }
        RS4GenericInfo(symbol.name,
                       signature) to LibrarySummary.RLibrarySymbol.FunctionRepresentation.ExtraNamedArguments.getDefaultInstance()
      }
      LibrarySummary.RLibrarySymbol.Type.S4METHOD -> {
        val methodsParameters = functionRepresentation.s4ParametersInfo.s4MethodParametersList.map {
          RS4MethodParameterInfo(it.name, it.type)
        }
        RS4RawMethodInfo(symbol.name,
                         methodsParameters) to LibrarySummary.RLibrarySymbol.FunctionRepresentation.ExtraNamedArguments.getDefaultInstance()
      }
      else -> null to functionRepresentation.extraNamedArguments
    }
  }
}