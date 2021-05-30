/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package org.jetbrains.r.classes.s4.extra.stubs

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.jetbrains.r.classes.s4.classInfo.RS4ClassInfo
import org.jetbrains.r.classes.s4.methods.RS4GenericOrMethodInfo
import org.jetbrains.r.psi.api.RCallExpression

class RCallExpressionStubImpl(parent: StubElement<*>,
                              stubElementType: IStubElementType<*, *>,
                              override val s4ClassInfo: RS4ClassInfo?,
                              override val s4GenericOrMethodInfo: RS4GenericOrMethodInfo?)
  : StubBase<RCallExpression>(parent, stubElementType), RCallExpressionStub {

  override fun toString(): String {
    return "RCallExpressionStub(${s4ClassInfo?.className})"
  }
}