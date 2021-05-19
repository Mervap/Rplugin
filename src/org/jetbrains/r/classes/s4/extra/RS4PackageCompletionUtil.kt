/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4.extra

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import org.jetbrains.r.classes.s4.extra.index.RS4GenericIndex
import org.jetbrains.r.editor.completion.RLookupElementFactory

object RS4PackageCompletionUtil {
  fun processElementsFromIndex(project: Project,
                               scope: GlobalSearchScope,
                               elementFactory: RLookupElementFactory,
                               consumer: (LookupElement, VirtualFile) -> Unit) {
    RS4GenericIndex.processAll(project, scope, Processor {
      consumer(elementFactory.createS4GenericLookupElement(it), it.containingFile.virtualFile)
      return@Processor true
    })
  }
}