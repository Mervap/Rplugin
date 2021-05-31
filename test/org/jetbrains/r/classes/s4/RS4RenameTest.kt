/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

// Copyright (c) 2017, Holger Brandl, Ekaterina Tuzova
package org.jetbrains.r.classes.s4

import org.jetbrains.r.rename.RRenameBaseTest

class RS4RenameTest : RRenameBaseTest() {

  fun testRenameS4Class() = doTestWithProject("YourClass")

  fun testRenameS4ClassFromContains() = doTestWithProject("Fruit")

  fun testRenameS4ClassFromNew() = doTestWithProject("Fruit")

  fun testRenameLibS4Class() = doTestWithProject("number", false)

  fun testRenameS4Slot() = doTestWithProject("address")

  fun testRenameS4SlotFromNew() = doTestWithProject("address")

  fun testRenameS4SlotFromAtExpr() = doTestWithProject("address")

  fun testRenameS4StringSlot() = doTestWithProject("address")

  fun testRenameS4StringSlotFromAtExpr() = doTestWithProject("address")

  fun testRenameLibS4Slot() = doTestWithProject("source", false)

  fun testRenameS4Generic() = doTestWithProject("bar")

  fun testRenameS4GenericFromMethod() = doTestWithProject("bar")

  fun testRenameS4GenericFromMethodDeclaration() = doTestWithProject("bar")
}
