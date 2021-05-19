/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.util.SystemInfo
import com.intellij.pom.PomTargetPsiElement
import org.jetbrains.concurrency.runAsync
import org.jetbrains.r.classes.s4.classInfo.RStringLiteralPomTarget
import org.jetbrains.r.psi.api.RIdentifierExpression
import org.jetbrains.r.resolve.RResolveFromFilesTestCase

class RS4IncludedSourcesResolveTest : RResolveFromFilesTestCase("resolveInS4Source") {

  override fun setUp() {
    super.setUp()
    addLibraries()
  }

  fun testS4Class() = doTest()

  fun testS4Slot() = doTest()

  private fun doTest() {
    val expected = getExpectedResult("# this").map {
     PsiElementWrapper((it.elem as? RIdentifierExpression)?.parent ?: it.elem)
    }.toSet()
    val actual = getActualResultsWithTimeLimit().map {
      when (it.elem) {
        is PomTargetPsiElement -> PsiElementWrapper((it.elem.target as RStringLiteralPomTarget).literal)
        else -> it
      }
    }.toSet()
    assertEquals(expected, actual)
  }

  private fun getActualResultsWithTimeLimit(): Set<PsiElementWrapper> {
    val result = runAsync { runReadAction { getActualResults() } }.blockingGet(if (SystemInfo.isWindows) 800 else 500)
    assertNotNull("Resolve is too long", result)
    return result!!
  }
}