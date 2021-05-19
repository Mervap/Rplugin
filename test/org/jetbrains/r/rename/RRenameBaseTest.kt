// Copyright (c) 2017, Holger Brandl, Ekaterina Tuzova
/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.rename

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.testFramework.fixtures.CodeInsightTestUtil
import org.jetbrains.r.RBundle
import org.jetbrains.r.RFileType.DOT_R_EXTENSION
import org.jetbrains.r.RLightCodeInsightFixtureTestCase
import org.jetbrains.r.classes.s4.extra.rename.RVariableInplaceRenameHandler
import org.jetbrains.r.refactoring.rename.RMemberInplaceRenameHandler
import org.jetbrains.r.rmarkdown.RMarkdownFileType
import java.io.File

abstract class RRenameBaseTest : RLightCodeInsightFixtureTestCase() {

  override fun setUp() {
    super.setUp()
    addLibraries()
  }

  protected fun doTestWithProject(newName: String, isInlineAvailable: Boolean = true, isRmd: Boolean = false, isSourceTest: Boolean = false) {
    val dotFileExtension = getDotExtension(isRmd)
    lateinit var startFiles: List<String>
    lateinit var endFiles: List<String>
    if (isSourceTest) {
      addLibraries()
      val files = File(myFixture.testDataPath + "/rename/" + getTestName(true))
                    .listFiles()
                    ?.map { it.absolutePath.replace(myFixture.testDataPath, "") }
                    ?.sortedByDescending { it.contains("main") } ?: error("Cannot find root test directory")
      startFiles = files.filter { !it.contains(".after.") }
      endFiles = files - startFiles
      myFixture.configureByFiles(*startFiles.toTypedArray())
    }
    else {
      myFixture.configureByFile("rename/" + getTestName(true) + dotFileExtension)
    }
    val variableHandler = RVariableInplaceRenameHandler()
    val memberHandler = RMemberInplaceRenameHandler()

    val element = TargetElementUtil.findTargetElement(myFixture.editor, TargetElementUtil.getInstance().getAllAccepted())

    assertNotNull(element)
    val dataContext = SimpleDataContext.getSimpleContext(CommonDataKeys.PSI_ELEMENT, element!!, createDataContext())
    val handler = when {
      memberHandler.isRenaming(dataContext) -> memberHandler
      variableHandler.isRenaming(dataContext) -> variableHandler
      else -> {
        assertFalse("In-place rename not allowed for $element", isInlineAvailable)
        return
      }
    }

    CodeInsightTestUtil.doInlineRename(handler, newName, myFixture)
    if (isSourceTest) {
      if (endFiles.size != startFiles.size) error("Different number of start and end files")
      for (i in startFiles.indices) {
        myFixture.checkResultByFile(startFiles[i], endFiles[i], false)
      }
    }
    else {
      myFixture.checkResultByFile("rename/" + getTestName(true) + ".after$dotFileExtension", true)
    }
  }

  protected fun doExceptionTestWithProject(newName: String,
                                         isFunctionCollision: Boolean,
                                         functionScope: String? = null,
                                         isInlineAvailable: Boolean = true,
                                         isRmd: Boolean = false,
                                         fileScope: String = getTestName(true) + getDotExtension(isRmd),
                                         isSourceTest: Boolean = false) {
    val scopeString =
      if (functionScope != null) RBundle.message("rename.processor.function.scope", functionScope)
      else RBundle.message("rename.processor.file.scope", fileScope)
    val message =
      if (isFunctionCollision) RBundle.message("rename.processor.collision.function.description", newName, scopeString)
      else RBundle.message("rename.processor.collision.variable.description", newName, scopeString)

    assertThrows(BaseRefactoringProcessor.ConflictsInTestsException::class.java, message) {
      doTestWithProject(newName, isInlineAvailable, isRmd, isSourceTest)
    }
  }

  private fun getDotExtension(isRmd: Boolean): String {
    val fileExtension = if (isRmd) RMarkdownFileType.defaultExtension.toLowerCase() else DOT_R_EXTENSION.drop(1)
    return ".$fileExtension"
  }
}
