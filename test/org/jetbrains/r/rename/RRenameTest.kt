// Copyright (c) 2017, Holger Brandl, Ekaterina Tuzova
/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.rename

class RRenameTest : RRenameBaseTest() {

  fun testRenameFunction() = doTestWithProject("test_function1")

  fun testRenameFunctionUsage() = doTestWithProject("test_function1")

  fun testRenameParameter() = doTestWithProject("x1")

  fun testRenameParameterUsage() = doTestWithProject("x1")

  fun testRenameLocalVariable() = doTestWithProject("ttt1")

  fun testRenameLocalVariableUsage() = doTestWithProject("ttt1")

  fun testRenameLocalVariableClosure() = doTestWithProject("ttt1")

  fun testRenameLocalVariableClosureUsage() = doTestWithProject("ttt1")

  fun testRenameForLoopTarget() = doTestWithProject("k")

  fun testRenameForLoopTargetUsage() = doTestWithProject("l")

  fun testRenameQuotedVariable() = doTestWithProject("New value")

  fun testRenameQuotedUnquotedVariable() = doTestWithProject("var")

  fun testRenameNeedQuote() = doTestWithProject("New val")

  fun testRenameLibraryFunction() = doTestWithProject("printt", false)

  fun testRenameRedeclarationGlobalInFunction() = doTestWithProject("global")

  fun testRenameVariableInFileCollisions() = doExceptionTestWithProject("was", false)

  fun testRenameVariableInFunctionCollisions() = doExceptionTestWithProject("was", false, "scopeFun")

  fun testRenameFunctionInFileCollisions() = doExceptionTestWithProject("was", true)

  fun testRenameFunctionInFunctionCollisions() = doExceptionTestWithProject("was", true, "scopeFun")

  fun testRenameRFile() {
    val psiFile = myFixture.configureByText("foo.R", "print('Hello world')")
    myFixture.renameElement(psiFile, "bar.R")
    assertEquals(psiFile.name, "bar.R")
  }

  fun testRenameFunctionInRmd() = doTestWithProject("test_function_rmd", isRmd = true)

  fun testRenameFunctionUsageInRmd() = doTestWithProject("test_function_rmd", isRmd = true)

  fun testRenameParameterInRmd() = doTestWithProject("x1", isRmd = true)

  fun testRenameParameterUsageInRmd() = doTestWithProject("x1", isRmd = true)

  fun testRenameLocalVariableInRmd() = doTestWithProject("ttt1", isRmd = true)

  fun testRenameLocalVariableUsageInRmd() = doTestWithProject("ttt1", isRmd = true)

  fun testRenameForLoopTargetInRmd() = doTestWithProject("k", isRmd = true)

  fun testRenameForLoopTargetUsageInRmd() = doTestWithProject("l", isRmd = true)

  fun testRenameDeclarationInSource() = doTestWithProject("bar", isSourceTest = true)

  fun testRenameDeclarationInSourceCollisions() = doExceptionTestWithProject("x", false, fileScope = "B.R", isSourceTest = true)

  fun testRenameOperator() = doTestWithProject("%sum%")

  fun testRenameDocumentationParam() = doTestWithProject("aaaa")

  fun testRenameDocumentationFunctionLink() = doTestWithProject("baz")
}
