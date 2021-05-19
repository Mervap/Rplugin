/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.completion

import org.jetbrains.r.console.RConsoleRuntimeInfoImpl
import org.jetbrains.r.console.addRuntimeInfo
import org.jetbrains.r.run.RProcessHandlerBaseTestCase

abstract class IdentifierBaseCompletionTest : RProcessHandlerBaseTestCase() {

  override fun setUp() {
    super.setUp()
    addLibraries()
  }

  protected fun doTest(text: String, vararg variants: String, strict: Boolean = false, withRuntimeInfo: Boolean = false) {
    myFixture.configureByText("foo.R", text)
    if (withRuntimeInfo) {
      myFixture.file.addRuntimeInfo(RConsoleRuntimeInfoImpl(rInterop))
    }
    val result = myFixture.completeBasic()
    assertNotNull(result)
    val lookupStrings = result.map { it.lookupString }
    if (strict) {
      assertOrderedEquals(lookupStrings, *variants)
    }
    else {
      assertContainsOrdered(lookupStrings, *variants)
    }
  }
}
