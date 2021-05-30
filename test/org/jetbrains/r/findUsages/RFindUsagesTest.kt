/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.findUsages

class RFindUsagesTest : RFindUsagesBaseTest() {

  override fun setUp() {
    super.setUp()
    addLibraries()
  }

  fun testLocalVariable() {
    doTest("""
      my.local.<caret>variable <- 10
      print(my.local.variable)
      print("hello")
      some.function <- function() {
        print(my.local.variable + 20)
      }
    """, """
      <root> (2)
       Variable
        my.local.variable
       Usages in Project Files (2)
        Unclassified (2)
         light_idea_test_case (2)
           (2)
           test.R (2)
            2print(my.local.variable)
            5print(my.local.variable + 20)
    """)
  }

  //TODO: Fix R-334
  fun testLocalFunction() {
    doTest("""
      my.local.<caret>function <- function(x, y) x + y
      print(my.local.function(2, 3))
      print("hello")
      some.other.function <- function() {
        print(my.local.function(4, 5))
      }
    """, """
      <root> (2)
       Function
        my.local.function
       Usages in Project Files (2)
        Unclassified (2)
         light_idea_test_case (2)
           (2)
           test.R (2)
            2print(my.local.function(2, 3))
            5print(my.local.function(4, 5))
     """)
  }

  fun testLibraryFunction() {
    doTest("""
      base.package <- packageDescription("base")      
      dplyr.package <- package<caret>Description("dplyr")      
    """, """
      <root> (2)
       Function
        packageDescription(pkg, lib.loc = NULL, fields = NULL, drop = TRUE, encoding = "")
       Usages in Project Files (2)
        Unclassified (2)
         light_idea_test_case (2)
           (2)
           test.R (2)
            1base.package <- packageDescription("base")      
            2dplyr.package <- packageDescription("dplyr")
     """)
  }

  fun testParameter() {
    doTest("""
      func <- function(x<caret>, y, z) {
        x + y + z
      }
      
      x <- 15
      p <- x + 10
      func(x = p)
    """, """
      <root> (2)
       Function parameter
        x
       Usages in Project Files (2)
        Unclassified (2)
         light_idea_test_case (2)
           (2)
           test.R (2)
            2x + y + z
            7func(x = p)
     """)
  }

  fun testRoxygenParameter() {
    doTest("""
      #' Title
      #' 
      #' Description
      #'
      #' @param x, y X and y
      #' @param z Z
      #' @example
      #' #' @param x,y,z Params
      func <- function(x<caret>, y, z) {
        x + y + z
      }
    """, """
      <root> (2)
       Function parameter
        x
       Usages in Project Files (2)
        Unclassified (1)
         light_idea_test_case (1)
           (1)
           test.R (1)
            10x + y + z
        Usage in roxygen2 documentation (1)
         light_idea_test_case (1)
           (1)
           test.R (1)
            5#' @param x, y X and y
     """)
  }

  fun testRoxygenHelpPageLink() {
    doTest("""
      #' Title
      #' 
      #' Description
      #'
      #' @see [bar]
      #' [bar][baz]
      #' [bar](baz)
      #' <bar:bar>
      func <- function(x, y, z) {
        bar(x) + y + z
      }
      
      ba<caret>r <- function(x) { x + 42 }
    """, """
      <root> (2)
       Function
        bar
       Usages in Project Files (2)
        Unclassified (1)
         light_idea_test_case (1)
           (1)
           test.R (1)
            10bar(x) + y + z
        Usage in roxygen2 documentation (1)
         light_idea_test_case (1)
           (1)
           test.R (1)
            5#' @see [bar]
     """)
  }
}