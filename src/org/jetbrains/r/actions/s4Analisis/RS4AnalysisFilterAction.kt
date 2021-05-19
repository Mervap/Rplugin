package org.jetbrains.r.actions.s4Analysis

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import com.intellij.util.io.createFile
import com.intellij.util.io.exists
import com.intellij.util.io.readText
import org.jetbrains.concurrency.runAsync
import org.jetbrains.r.actions.REditorActionBase
import org.jetbrains.r.psi.RElementFactory
import org.jetbrains.r.psi.RRecursiveElementVisitor
import org.jetbrains.r.psi.api.RCallExpression
import org.jetbrains.r.psi.api.RFile
import org.jetbrains.r.psi.isFunctionFromLibrarySoft
import java.nio.charset.MalformedInputException
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.swing.Icon
import kotlin.io.path.*

@ExperimentalPathApi
class RS4AnalysisFilterAction : REditorActionBase {
  constructor() : super()

  constructor(text: String, description: String, icon: Icon?) : super(text, description, icon)

  private val root = Path.of("/", "home", "All_r_files")
  private val out = root.resolve("out")
  private val info = root.resolve("data_description_R.csv")

  override fun actionPerformed(e: AnActionEvent) {
    out.createDirectories()
    val project = e.project
    runBackgroundableTask("S4 filtering", e.project) { progress ->
      var i = -1
      var j = 14635
      info.forEachDataEntry { sourcePath, repositoryUrl, path ->
        i += 1
        progress.fraction = i.toDouble() / 4010530
        progress.text = sourcePath
        if (i < 4007347) return@forEachDataEntry
        out.resolve("process.txt").writeText(i.toString() + "\n")
        try {
          val source = root.resolve(sourcePath).readText().replace("\r\n", "\n")
          val file = runAsync { RElementFactory.buildRFileFromText(project, source) as RFile }.blockingGet(15, TimeUnit.SECONDS)!!
          if (file.firstChild.elementType == TokenType.BAD_CHARACTER) {
            throw MalformedInputException(source.length)
          }
          RS4Visitor(out, repositoryUrl, path).visitElement(file)
        }
        catch (e: MalformedInputException) {
          j += 1
          out.resolve("bad.txt").writeText(j.toString() + "\n")
          return@forEachDataEntry
        }
        catch (e: TimeoutException) {
          j += 1
          out.resolve("bad.txt").writeText(j.toString() + "\n")
          val timeout = out.resolve("timeout.txt")
          if (!timeout.exists()) timeout.createFile()
          timeout.appendText(sourcePath + "\n")
          return@forEachDataEntry
        }
      }
    }
  }
}

@ExperimentalPathApi
private fun Path.forEachDataEntry(columns: Int = 4, action: (sourcePath: String, repositoryUrl: String, path: String) -> Unit) {
  var parts = mutableListOf<String>()
  forEachLine {
    var newParts = it.split(";")
    if (newParts.size > 4) {
      newParts = newParts.take(2) + newParts.drop(2).dropLast(1).joinToString(";") + newParts.takeLast(1)
    }
    if (parts.isNotEmpty()) {
      val last = parts.removeLast()
      parts.add(last + newParts.first())
      parts.addAll(newParts.drop(1))
    } else {
      parts.addAll(newParts)
    }
    if (parts.size >= columns) {
      val (sourcePath, repositoryUrl, path) = parts.take(columns)
      runReadAction { action(sourcePath, repositoryUrl, path) }
      parts = parts.drop(columns).toMutableList()
    }
  }
}

@ExperimentalPathApi
private class RS4Visitor(val out: Path, val repositoryUrl: String, val path: String) : RRecursiveElementVisitor() {
  override fun visitCallExpression(o: RCallExpression) {
    super.visitCallExpression(o)
    val outName = when {
      o.isFunctionFromLibrarySoft("setClass", "methods") -> "setClass"
      o.isFunctionFromLibrarySoft("setGeneric", "methods") -> "setGeneric"
      o.isFunctionFromLibrarySoft("setMethod", "methods") -> "setMethod"
      else -> null
    } ?: return

    val text = "# $repositoryUrl/tree/master/$path\n${o.text}\n\n"
    val file = out.resolve("$outName.R")
    if (!file.exists()) file.createFile()
    file.appendText(text)
  }
}