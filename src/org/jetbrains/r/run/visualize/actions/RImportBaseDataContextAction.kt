/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.run.visualize.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.r.RBundle
import org.jetbrains.r.rinterop.RInterop
import org.jetbrains.r.run.visualize.RImportBaseDataDialog

class RImportBaseDataContextAction : RImportDataContextAction(TITLE, DESCRIPTION) {
  override fun applyTo(project: Project, interop: RInterop, file: VirtualFile) {
    RImportBaseDataDialog(project, interop, project, file.path).show()
  }

  companion object {
    private val DESCRIPTION = RBundle.message("import.data.action.base.description")
    private val TITLE = RBundle.message("import.data.action.base.title")
  }
}
