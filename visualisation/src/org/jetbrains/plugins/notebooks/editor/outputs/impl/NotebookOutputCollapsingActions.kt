package org.jetbrains.plugins.notebooks.editor.outputs.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.DumbAware
import com.intellij.util.castSafelyTo
import org.jetbrains.plugins.notebooks.editor.NotebookCellInlayManager
import org.jetbrains.plugins.notebooks.editor.NotebookCellLines
import org.jetbrains.plugins.notebooks.editor.notebookCellLinesInterval
import org.jetbrains.plugins.notebooks.editor.outputs.NotebookOutputInlayController

internal class NotebookOutputCollapseAllAction private constructor() : ToggleAction(), DumbAware {
  override fun update(e: AnActionEvent) {
    super.update(e)
    e.presentation.isEnabledAndVisible = e.notebookEditor != null
  }

  override fun isSelected(e: AnActionEvent): Boolean =
    !allCollapsingComponents(e).any { it.isSeen }

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    for (component in allCollapsingComponents(e)) {
      component.isSeen = !state
    }
  }

  private fun allCollapsingComponents(e: AnActionEvent): Sequence<CollapsingComponent> {
    val inlayManager = e.notebookCellInlayManager ?: return emptySequence()
    return NotebookCellLines.get(inlayManager.editor).intervalsIterator().asSequence()
      .filter { it.type == NotebookCellLines.CellType.CODE }
      .mapNotNull { getCollapsingComponents(inlayManager.editor, it) }
      .flatMap { it }
  }
}

internal class NotebookOutputCollapseAllInCellAction private constructor() : ToggleAction(), DumbAware {
  override fun update(e: AnActionEvent) {
    super.update(e)
    e.presentation.isEnabledAndVisible = getCollapsingComponents(e) != null
  }

  override fun isSelected(e: AnActionEvent): Boolean {
    val collapsingComponents = getCollapsingComponents(e) ?: return false
    return collapsingComponents.isNotEmpty() && collapsingComponents.all { !it.isSeen }
  }

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    getCollapsingComponents(e)?.forEach {
      it.isSeen = !state
    }
  }
}

internal class NotebookOutputCollapseSingleInCellAction private constructor() : ToggleAction(), DumbAware {
  override fun update(e: AnActionEvent) {
    super.update(e)
    e.presentation.isEnabledAndVisible = getCollapsingComponents(e) != null
  }

  override fun isSelected(e: AnActionEvent): Boolean =
    getExpectedComponent(e)?.isSeen?.let { !it } ?: false

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    getExpectedComponent(e)?.isSeen = !state
  }

  private fun getExpectedComponent(e: AnActionEvent): CollapsingComponent? =
    e.getData(PlatformDataKeys.CONTEXT_COMPONENT)
      ?.castSafelyTo<CollapsingComponent>()
      ?.let { expectedComponent ->
        getCollapsingComponents(e)?.singleOrNull { it === expectedComponent }
      }
}

private fun getCollapsingComponents(e: AnActionEvent): List<CollapsingComponent>? {
  val interval = e.dataContext.notebookCellLinesInterval ?: return null
  return e.getData(PlatformDataKeys.EDITOR)?.let { getCollapsingComponents(it, interval) }
}

private fun getCollapsingComponents(editor: Editor, interval: NotebookCellLines.Interval): List<CollapsingComponent>? =
  NotebookCellInlayManager.get(editor)
    ?.inlaysForInterval(interval)
    ?.filterIsInstance<NotebookOutputInlayController>()
    ?.firstOrNull()
    ?.collapsingComponents

private val AnActionEvent.notebookCellInlayManager: NotebookCellInlayManager?
  get() = getData(PlatformDataKeys.EDITOR)?.let(NotebookCellInlayManager.Companion::get)

private val AnActionEvent.notebookEditor: EditorImpl?
  get() = notebookCellInlayManager?.editor