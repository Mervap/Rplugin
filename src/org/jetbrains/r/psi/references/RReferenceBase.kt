/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.psi.references

import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.IncorrectOperationException
import org.jetbrains.r.classes.s4.extra.RS4RReferenceBase
import org.jetbrains.r.console.runtimeInfo
import org.jetbrains.r.packages.RPackage
import org.jetbrains.r.psi.api.RPsiElement
import org.jetbrains.r.skeleton.RSkeletonFileType

abstract class RReferenceBase<T : RPsiElement>(protected val psiElement: T) : PsiPolyVariantReference {

  @Throws(IncorrectOperationException::class)
  override fun bindToElement(element: PsiElement) = null

  override fun isReferenceTo(element: PsiElement): Boolean {
    if (RS4RReferenceBase.isReferenceTo(psiElement, element)) return true
    return when (val resolve = resolve()) {
      is PomTargetPsiElement -> RS4RReferenceBase.isReferenceTo(element, resolve)
      is PsiNameIdentifierOwner -> resolve === element || resolve.identifyingElement === element
      else -> resolve === element
    }
  }

  override fun isSoft() = false

  override fun getCanonicalText() = element.text!!

  override fun getElement() = psiElement

  override fun getRangeInElement() = psiElement.node.textRange.shiftRight(-psiElement.node.startOffset)

  override fun handleElementRename(newElementName: String): PsiElement? = null

  override fun resolve(): PsiElement? {
    val results = multiResolve(false)
    return if (results.size == 1) results.first().element else null
  }

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    return ResolveCache.getInstance(psiElement.project).resolveWithCaching(this, Resolver<T>(), false, incompleteCode)
  }

  /**
   * @return false if all resolve targets are package members and these packages are not loaded
   */
  fun areTargetsLoaded(incompleteCode: Boolean) : Boolean {
    val runtimeInfo = psiElement.containingFile?.runtimeInfo ?: return true
    return multiResolve(incompleteCode).all { result ->
      val name = findPackageNameByResolveResult(result) ?: return@all true
      runtimeInfo.loadedPackages.keys.contains(name)
    }
  }

  protected abstract fun multiResolveInner(incompleteCode: Boolean): Array<ResolveResult>

  private class Resolver<T : RPsiElement> : ResolveCache.PolyVariantResolver<RReferenceBase<T>> {
    override fun resolve(reference: RReferenceBase<T>, incompleteCode: Boolean): Array<ResolveResult> {
      val resolveResults = reference.multiResolveInner(incompleteCode)
      val element = reference.psiElement
      return RResolver.sortValidResolveResults(element, element.containingFile.runtimeInfo, resolveResults)
    }
  }

  companion object {
    fun findPackageNameByResolveResult(result: ResolveResult): String? =
      result.element?.containingFile?.takeIf { it.fileType == RSkeletonFileType }?.let { RPackage.getOrCreateRPackageBySkeletonFile(it)?.name }
  }
}