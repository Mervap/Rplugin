/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4

import org.jetbrains.r.findUsages.RFindUsagesBaseTest

class RS4FindUsagesTest : RFindUsagesBaseTest() {

  override fun setUp() {
    super.setUp()
    addLibraries()
  }

  fun testS4Class() {
    doTest("""
      setClass('MyCl<caret>ass', slots = c(slot = 'numeric'))
      setClass('MyClass1', contains = 'MyClass')
      
      obj <- new('MyClass', slot = 5)
      obj1 <- new('MyClass1', slot = 6)
    """, """
      <root> (2)
       S4 class
        MyClass
       Usages in Project Files (2)
        Unclassified (2)
         light_idea_test_case (2)
           (2)
           2setClass('MyClass1', contains = 'MyClass')
           4obj <- new('MyClass', slot = 5)
     """)
  }

  fun testS4Slot() {
    doTest("""
      setClass('MyClass', slots = c(s<caret>lot = 'numeric', slot1 = 'character'))
      setClass('MyClass1', contains = 'MyClass')
      
      obj <- new('MyClass', slot = 5, slot1 = 'hello')
      obj1 <- new('MyClass1', slot = 6, slot1 = 'world')
      obj@slot
      obj@slot1
      obj1@slot
      obj1@slot1
    """, """
      <root> (4)
       S4 slot
        slot
       Usages in Project Files (4)
        Unclassified (4)
         light_idea_test_case (4)
           (4)
           4obj <- new('MyClass', slot = 5, slot1 = 'hello')
           5obj1 <- new('MyClass1', slot = 6, slot1 = 'world')
           6obj@slot
           8obj1@slot
     """)
  }

  fun testS4ComplexSlot() {
    doTest("""
      setClass('MyClass', slots = c(s<caret>lot = c('numeric', ext = 'character')))
      setClass('MyClass1', contains = 'MyClass')
      
      obj <- new('MyClass', slot1 = 5, slot.ext = 'hello')
      obj1 <- new('MyClass1', slot.ext = 'world', slot1 = 6)
      obj@slot
      obj@slot1
      obj@slot.ext
      obj1@slot
      obj1@slot1
      obj1@slot.ext
    """, """
      <root> (6)
       S4 slot
        slot
       Usages in Project Files (6)
        Unclassified (6)
         light_idea_test_case (6)
           (6)
           4obj <- new('MyClass', slot1 = 5, slot.ext = 'hello')
           5obj1 <- new('MyClass1', slot.ext = 'world', slot1 = 6)
           7obj@slot1
           8obj@slot.ext
           10obj1@slot1
           11obj1@slot.ext
     """)
  }
}