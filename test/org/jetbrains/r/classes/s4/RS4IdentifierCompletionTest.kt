/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4

import org.jetbrains.r.completion.IdentifierBaseCompletionTest

class RS4IdentifierCompletionTest : IdentifierBaseCompletionTest() {

  override fun setUp() {
    super.setUp()
    addLibraries()
  }

  fun testLibS4Generic() {
    doTest("sho<caret>", "show")
  }

  fun testUserS4Generic() {
    doTest("""
      setGeneric("myShow", "obj", function(obj) standartGeneric("myShow"))
      setGeneric("myShow1", "obj1", function(obj1) standartGeneric("myShow1"))
      mySho<caret>
    """.trimIndent(), "myShow", "myShow1")
  }
}
