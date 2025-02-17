package org.jetbrains.r.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.util.TextRange
import com.intellij.util.EventDispatcher
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.plugins.notebooks.editor.NotebookCellLines
import org.jetbrains.plugins.notebooks.editor.NotebookCellLinesLexer

/**
 * inspired by [org.jetbrains.plugins.notebooks.editor.NotebookCellLinesImpl], bound to RMarkdown lexer implementation details
 */
class RMarkdownCellLines private constructor(private val document: Document,
                                             private val cellLinesLexer: NotebookCellLinesLexer) : NotebookCellLines {

  private var markers: List<NotebookCellLines.Marker> = emptyList()
  private var intervals: List<NotebookCellLines.Interval> = emptyList()
  private val documentListener = createDocumentListener()
  override val intervalListeners = EventDispatcher.create(NotebookCellLines.IntervalListener::class.java)

  override val intervalsCount: Int
    get() = intervals.size

  override var modificationStamp: Long = 0
    private set

  init {
    document.addDocumentListener(documentListener)
    updateIntervalsAndMarkers()
  }

  override fun getIterator(ordinal: Int): ListIterator<NotebookCellLines.Interval> =
    intervals.listIterator(ordinal)

  override fun getIterator(interval: NotebookCellLines.Interval): ListIterator<NotebookCellLines.Interval> {
    check(interval == intervals[interval.ordinal])
    return intervals.listIterator(interval.ordinal)
  }

  override fun intervalsIterator(startLine: Int): ListIterator<NotebookCellLines.Interval> {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val ordinal = intervals.find { startLine <= it.lines.last }?.ordinal ?: intervals.size
    return intervals.listIterator(ordinal)
  }

  override fun markersIterator(startOffset: Int): ListIterator<NotebookCellLines.Marker> {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val ordinal = markers.find { startOffset == it.offset || startOffset < it.offset + it.length }?.ordinal ?: markers.size
    return markers.listIterator(ordinal)
  }

  private fun updateIntervalsAndMarkers() {
    markers = cellLinesLexer.markerSequence(document.charsSequence, 0, 0).toList()
    // for RMarkdown markers offset + length == nextMarker.offset, actually markers is intervals
    intervals = markers.map(this::toInterval)
  }

  private fun toInterval(marker: NotebookCellLines.Marker): NotebookCellLines.Interval {
    val startLine = document.getLineNumber(marker.offset)

    val endLine =
      if (marker.length == 0) startLine
      else document.getLineNumber(marker.offset + marker.length - 1)

    return NotebookCellLines.Interval(
      ordinal = marker.ordinal,
      type = marker.type,
      lines = startLine..endLine
    )
  }

  private fun notifyChanged(oldCells: List<NotebookCellLines.Interval>,
                            oldAffectedCells: List<NotebookCellLines.Interval>,
                            newCells: List<NotebookCellLines.Interval>,
                            newAffectedCells: List<NotebookCellLines.Interval>) {
    if (oldCells == newCells) {
      return
    }

    val trimAtBegin = oldCells.zip(newCells).takeWhile { (oldCell, newCell) ->
      oldCell == newCell &&
      oldCell != oldAffectedCells.firstOrNull() && newCell != newAffectedCells.firstOrNull()
    }.count()

    val trimAtEnd = oldCells.asReversed().zip(newCells.asReversed()).takeWhile { (oldCell, newCell) ->
      oldCell.type == newCell.type && oldCell.size == newCell.size &&
      oldCell != oldAffectedCells.lastOrNull() && newCell != newAffectedCells.lastOrNull()
    }.count()

    intervalListeners.multicaster.segmentChanged(
      trimmed(oldCells, trimAtBegin, trimAtEnd),
      trimmed(newCells, trimAtBegin, trimAtEnd)
    )
  }

  private fun createDocumentListener() = object : DocumentListener {
    private var oldAffectedCells: List<NotebookCellLines.Interval> = emptyList()

    override fun beforeDocumentChange(event: DocumentEvent) {
      oldAffectedCells = getAffectedCells(intervals, document, TextRange(event.offset, event.offset + event.oldLength))
    }

    override fun documentChanged(event: DocumentEvent) {
      ApplicationManager.getApplication().assertWriteAccessAllowed()
      val oldIntervals = intervals
      updateIntervalsAndMarkers()

      val newAffectedCells = getAffectedCells(intervals, document, TextRange(event.offset, event.offset + event.newLength))
      notifyChanged(oldIntervals, oldAffectedCells, intervals, newAffectedCells)
    }
  }

  companion object {
    private val map = ContainerUtil.createConcurrentWeakMap<Document, NotebookCellLines>()

    fun get(document: Document, lexerProvider: NotebookCellLinesLexer): NotebookCellLines =
      map.computeIfAbsent(document) {
        RMarkdownCellLines(document, lexerProvider)
      }
  }
}

private fun <T> trimmed(list: List<T>, trimAtBegin: Int, trimAtEnd: Int) =
  list.subList(trimAtBegin, list.size - trimAtEnd)

private val NotebookCellLines.Interval.size: Int
  get() = lines.last + 1 - lines.first

private fun getAffectedCells(intervals: List<NotebookCellLines.Interval>,
                             document: Document,
                             textRange: TextRange): List<NotebookCellLines.Interval> {
  val firstLine = document.getLineNumber(textRange.startOffset)
  val endLine = document.getLineNumber(textRange.endOffset)

  return intervals.dropWhile {
    it.lines.last < firstLine
  }.takeWhile {
    it.lines.first <= endLine
  }
}