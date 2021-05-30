/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4.extra.stubs

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.util.IncorrectOperationException
import org.jetbrains.r.classes.s4.classInfo.RS4ClassInfo
import org.jetbrains.r.psi.api.RArgumentList
import org.jetbrains.r.psi.api.RCallExpression
import org.jetbrains.r.psi.api.RExpression
import org.jetbrains.r.psi.references.RReferenceBase
import org.jetbrains.r.skeleton.psi.RSkeletonBase

class RSkeletonCallExpression(private val myStub: RSkeletonCallExpressionStub) : RSkeletonBase(), RCallExpression {
  override fun getMirror() = null

  override fun getParent(): PsiElement = myStub.parentStub.psi

  override fun getStub(): RSkeletonCallExpressionStub = myStub

  override fun getExpression(): RExpression {
    throw IncorrectOperationException("Operation not supported in: $javaClass")
  }

  override fun getElementType(): IStubElementType<out StubElement<*>, *> = stub.stubType

  override fun getName(): String = myStub.s4ClassInfo.className

  override fun canNavigate(): Boolean = false

  override fun getText(): String = stub.s4ClassInfo.getDeclarationText(project)

  override fun getArgumentList(): RArgumentList {
    throw IncorrectOperationException("Operation not supported in: $javaClass")
  }

  override fun getAssociatedS4ClassInfo(): RS4ClassInfo = myStub.s4ClassInfo

  override fun getReference(): RReferenceBase<*>? = null

  override fun navigate(requestFocus: Boolean) {}
}