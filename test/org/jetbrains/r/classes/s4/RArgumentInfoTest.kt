/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.r.RLightCodeInsightFixtureTestCase
import org.jetbrains.r.hints.parameterInfo.RArgumentInfo
import org.jetbrains.r.psi.api.RCallExpression
import org.jetbrains.r.psi.api.RExpression
import org.jetbrains.r.psi.api.RNamedArgument

class RArgumentInfoTest : RLightCodeInsightFixtureTestCase() {

  fun testArgumentPermutationInfoExist() {
    val info = argumentPermutationInfoForCall("""
      foo <- function() 42
      fo<caret>o()
    """.trimIndent())
    assertNotNull(info)
  }

  fun testArgumentPermutationInfoNotExist() {
    val info = argumentPermutationInfoForCall("foo()")
    assertNull(info)
  }

  private fun doTest1(text: String, vararg ind: Int, isValid: Boolean) {
    val info = argumentPermutationInfoForCall(text)
    assertNotNull(info)
    assertEquals(ind.toList(), info!!.argumentPermutationInd)
    assertEquals(ind.toList(), info.argumentPermutationIndWithPipeExpression)
    assertEquals(isValid, info.isValid)
  }

  fun testArgumentPermutation1() {
    doTest1("""
      foo <- function(a, b, c) 42
      fo<caret>o(10, 20, 30)
    """.trimIndent(), 0, 1, 2, isValid = true)
  }

  fun testArgumentPermutation2() {
    doTest1("""
      foo <- function(a, b, c, d) 42
      fo<caret>o(10, a = 20, 30)
    """.trimIndent(), 1, 0, 2, isValid = true)
  }

  fun testArgumentPermutation3() {
    doTest1("""
      foo <- function(a, b, ..., c) 42
      fo<caret>o(10, 30, 40, r = 50, c = 20, 50, 60, a = 30)
    """.trimIndent(), 1, 2, 2, 2, 3, 2, 2, 0, isValid = true)
  }

  fun testArgumentPermutation4() {
    doTest1("""
      foo <- function(a, b, c) 42
      fo<caret>o(10, d = 30, c = 40, 20, 50, 60)
    """.trimIndent(), 0, -1, 2, 1, -1, -1, isValid = false)
  }

  private fun doTest2(text: String, vararg ind: Int, isValid: Boolean) {
    val info = argumentPermutationInfoForCall(text)
    assertNotNull(info)
    assertEquals(ind.toList().drop(1), info!!.argumentPermutationInd)
    assertEquals(ind.toList(), info.argumentPermutationIndWithPipeExpression)
    assertEquals(isValid, info.isValid)
  }


  fun testArgumentPermutationWithPipe1() {
    doTest2("""
      foo <- function(a, b, c) 42
      10 %>% fo<caret>o(20, 30)
    """.trimIndent(), 0, 1, 2, isValid = true)
  }

  fun testArgumentPermutationWithPipe2() {
    doTest2("""
      foo <- function(a, b, c, d) 42
      10 %>% fo<caret>o(a = 20, 30)
    """.trimIndent(), 1, 0, 2, isValid = true)
  }

  fun testArgumentPermutationWithPipe3() {
    doTest2("""
      foo <- function(a, b, ..., c) 42
      10 %>% fo<caret>o(30, l = 40, c = 20, 50, 60, a = 30)
    """.trimIndent(), 1, 2, 2, 3, 2, 2, 0, isValid = true)
  }

  fun testArgumentPermutationWithPipe4() {
    doTest2("""
      foo <- function(a, b, c) 42
      10 %>% fo<caret>o(d = 30, c = 40, 20, 50, 60)
    """.trimIndent(), 0, -1, 2, 1, -1, -1, isValid = false)
  }

  fun testArgumentPermutationWithPipe5() {
    doTest2("""
      foo <- function() 42
      10 %>% fo<caret>o(a = 30, 20)
    """.trimIndent(), -1, -1, -1, isValid = false)
  }

  fun testArgumentPermutationWithPipe6() {
    doTest2("""
      foo <- function(a) 42
      10 %>% fo<caret>o(a = 30)
    """.trimIndent(), -1, 0, isValid = false)
  }

  private fun doTest3(text: String, vararg names: String?) {
    val info = argumentPermutationInfoForCall(text)
    assertNotNull(info)
    assertEquals(names.toList(), info!!.argumentNames)
  }


  fun testArgumentNames1() {
    doTest3("""
      foo <- function(a, b, c) 42
      fo<caret>o(10, 20, 30)
    """.trimIndent(), "a", "b", "c")
  }

  fun testArgumentNames2() {
    doTest3("""
      foo <- function(a, b, c, d) 42
      fo<caret>o(b = 10, 20, a = 30)
    """.trimIndent(), "b", "c", "a")
  }

  fun testArgumentNames3() {
    doTest3("""
      foo <- function(a, b, ..., d) 42
      fo<caret>o(b = 10, 20, 60, d = 30, 40, m = 50)
    """.trimIndent(), "b", "a", "...", "d", "...", "...")
  }

  fun testArgumentNames4() {
    doTest3("""
      foo <- function(a, b, c) 42
      fo<caret>o(10, d = 20, 30, 40, 50, c = 40)
    """.trimIndent(), "a", null, "b", null, null, "c")
  }

  private fun doTest4(text: String, vararg names: String?) {
    val info = argumentPermutationInfoForCall(text)
    assertNotNull(info)
    assertEquals(names.toList().drop(1), info!!.argumentNames)
    assertEquals(names.toList(), info.argumentNamesWithPipeExpression)
  }


  fun testArgumentNamesWithPipe1() {
    doTest4("""
      foo <- function(a, b, c) 42
      10 %>% fo<caret>o(20, 30)
    """.trimIndent(), "a", "b", "c")
  }

  fun testArgumentNamesWithPipe2() {
    doTest4("""
      foo <- function(a, b, c, d) 42
      20 %>% fo<caret>o(c = 10, a = 30)
    """.trimIndent(), "b", "c", "a")
  }

  fun testArgumentNamesWithPipe3() {
    doTest4("""
      foo <- function(a, b, ..., d) 42
      20 %>% fo<caret>o(b = 10, 60, d = 30, 40, u = 50)
    """.trimIndent(), "a", "b", "...", "d", "...", "...")
  }

  fun testArgumentNamesWithPipe4() {
    doTest4("""
      foo <- function(a) 42
      10 %>% fo<caret>o(10, a = 10, 20)
    """.trimIndent(), null, null, "a", null)
  }

  private fun doTest5(text: String, ind: List<Int>, names: List<String?>) {
    val info = argumentPermutationInfoForCall(text)
    assertNotNull(info)
    assertEquals(ind.toList(), info!!.notPassedParameterInd)
    assertEquals(names.toList(), info.notPassedParameterNames)
  }


  fun testNotPassedParameters1() {
    doTest5("""
      foo <- function(a, b, c) 42
      fo<caret>o(10, 20, 30)
    """.trimIndent(), emptyList(), emptyList())
  }

  fun testNotPassedParameters2() {
    doTest5("""
      foo <- function(a, b, c) 42
      fo<caret>o(b = 10)
    """.trimIndent(), listOf(0, 2), listOf("a", "c"))
  }

  fun testNotPassedParameters3() {
    doTest5("""
      foo <- function(a, b, ..., c) 42
      fo<caret>o(10, b = 10)
    """.trimIndent(), listOf(2, 3), listOf("...", "c"))
  }

  fun testNotPassedParameters4() {
    doTest5("""
      foo <- function(a, b, ..., c) 42
      fo<caret>o(10, b = 10, 20)
    """.trimIndent(), listOf(3), listOf("c"))
  }

  fun testNotPassedParameters5() {
    doTest5("""
      foo <- function(a, b, c) 42
      100 %>% fo<caret>o(c = 10)
    """.trimIndent(), listOf(1), listOf("b"))
  }

  fun testNotPassedParameters6() {
    doTest5("""
      foo <- function(a) 42
      42 %>% fo<caret>o(c = 10, a = 10, 10, 20, 30)
    """.trimIndent(), emptyList(), emptyList())
  }

  private fun doTest6(text: String, vararg ind: Int) {
    val call = findCallAtCaret(text)
    val info = argumentPermutationInfoForCall(text, call)
    assertNotNull(info)
    val expressions = info!!.expressionListWithPipeExpression
    val expectedResult = ind.map { expressions[it] }
    assertEquals(expectedResult, info.allDotsArguments)
    assertEquals(expectedResult, RArgumentInfo.getAllDotsArguments(call))
  }


  fun testAllDotsArguments1() {
    doTest6("""
      foo <- function(a, b, ..., c) 42
      fo<caret>o(10, 20, 30, b = 40, 50, c = 60, 90)
    """.trimIndent(), 1, 2, 4, 6)
  }

  fun testAllDotsArguments2() {
    doTest6("""
      foo <- function(a, b, ..., c) 42
      50 %>% fo<caret>o(10, l = 20, z = 30, b = 40, a = 50, c = 60, 90)
    """.trimIndent(), 0, 1, 2, 3, 7)
  }

  fun testAllDotsArguments3() {
    doTest6("""
      foo <- function(a, b) a + b
      fo<caret>o(10, 20)
    """.trimIndent())
  }

  private fun doTest7(text: String, vararg names: String?) {
    val info = argumentPermutationInfoForCall(text)
    assertNotNull(info)
    assertEquals(names.toList(), info!!.expressionListWithPipeExpression.map { info.getParameterNameForArgument(it) })
  }


  fun testGetArgumentParameterName1() {
    doTest7("""
      foo <- function(a, b, c) 42
      fo<caret>o(10, 20, 30)
    """.trimIndent(), "a", "b", "c")
  }

  fun testGetArgumentParameterName2() {
    doTest7("""
      foo <- function(a, b, ..., d) 42
      fo<caret>o(b = 10, 20, 30, 60, d = 80, 90)
    """.trimIndent(), "b", "a", "...", "...", "d", "...")
  }

  fun testGetArgumentParameterName3() {
    doTest7("""
      foo <- function(a, b, ..., d) 42
      42 %>% fo<caret>o(d = 10, 20, 30, a = 80, 90)
    """.trimIndent(), "b", "d", "...", "...", "a", "...")
  }

  fun testGetArgumentParameterName4() {
    doTest7("""
      foo <- function(a, b, ..., d) 42
      42 %>% fo<caret>o(d = 10, 20, l = 7, a = 80, 90)
    """.trimIndent(), "b", "d", "...", "...", "a", "...")
  }

  private fun doTest8(text: String, vararg names: String?) {
    val call = findCallAtCaret(text)
    val info = argumentPermutationInfoForCall(text, call)
    assertNotNull(info)
    val parameterNames = info!!.parameterNames
    val nameToExpr = names
      .mapIndexedNotNull { ind, name -> (name ?: return@mapIndexedNotNull null) to info.expressionListWithPipeExpression[ind] }
      .toMap()
    val expectedResult = parameterNames.map { nameToExpr[it]?.unfoldNamedArgument() }
    assertEquals(expectedResult, parameterNames.map { info.getArgumentPassedToParameter(it) })
    assertEquals(expectedResult, parameterNames.mapIndexed { ind, _ -> info.getArgumentPassedToParameter(ind) })
    assertEquals(expectedResult, parameterNames.map { RArgumentInfo.getArgumentByName(call, it) })
  }


  fun testGetArgumentPassedToParameter1() {
    doTest8("""
      foo <- function(a, b, c) 42
      fo<caret>o(10, 20, 30)
    """.trimIndent(), "a", "b", "c")
  }

  fun testGetArgumentPassedToParameter2() {
    doTest8("""
      foo <- function(a, b, c, d) 42
      fo<caret>o(b = 10, 20, a = 30, 60, d = 80, 90)
    """.trimIndent(), "b", "c", "a", null, "d", null)
  }

  fun testGetArgumentPassedToParameter3() {
    doTest8("""
      foo <- function(a, b, f) 42
      42 %>% fo<caret>o(10)
    """.trimIndent(), "a", "b")
  }

  private fun doTest9(text: String) {
    val call = findCallAtCaret(text)
    val info = argumentPermutationInfoForCall(text, call)
    assertNotNull(info)
    val expressions = call.argumentList.expressionList
    assertEquals(expressions.map { it.unfoldNamedArgument() },
                 expressions.map {
                   info!!.getArgumentPassedToParameter(info.getParameterNameForArgument(it) ?: error("Undefined argument ${it.text}"))
                 })

    val filteredNames = info!!.parameterNames.filter { info.getArgumentPassedToParameter(it) != null }
    assertEquals(filteredNames, filteredNames.map { info.getParameterNameForArgument(info.getArgumentPassedToParameter(it)!!) })
  }


  fun testCheckArgumentParameterNamePassedToParameterInverseOp1() {
    doTest9("""
      foo <- function(a, b, c, d, e) 42
      fo<caret>o(10, 20, a = 50, e = 10, 40)
    """.trimIndent())
  }

  fun testCheckArgumentParameterNamePassedToParameterInverseOp2() {
    doTest9("""
      foo <- function(a, b, c, d, e) 42
      42 %>% fo<caret>o(10, a = 50, e = 10, 40)
    """.trimIndent())
  }

  fun testInfoForLeftPipeExpr() {
    val info = argumentPermutationInfoForCall("""
      foo <- function(a) a
      bar <- function(a, b) a + b
      fo<caret>o(-42) %>% bar(42)
    """.trimIndent())
    assertNotNull(info)
    assertEquals(1, info!!.expressionListWithPipeExpression.size)
  }

  private fun argumentPermutationInfoForCall(text: String, call: RCallExpression = findCallAtCaret(text)): RArgumentInfo? {
    return RArgumentInfo.getArgumentInfo(call)
  }

  private fun findCallAtCaret(text: String): RCallExpression {
    val file = myFixture.configureByText("a.R", text)
    val element = file.findElementAt(myFixture.caretOffset)
    return PsiTreeUtil.getParentOfType(element, RCallExpression::class.java) ?: error("No RCallExpression at caret")
  }

  private fun RExpression.unfoldNamedArgument() = if (this is RNamedArgument) assignedValue else this
}