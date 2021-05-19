package org.jetbrains.r.actions.s4Analysis

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.util.io.createFile
import com.intellij.util.io.exists
import org.jetbrains.r.actions.REditorActionBase
import org.jetbrains.r.classes.s4.classInfo.RS4ClassInfoUtil
import org.jetbrains.r.classes.s4.classInfo.RS4ClassSlot
import org.jetbrains.r.hints.parameterInfo.RArgumentInfo
import org.jetbrains.r.psi.RElementFactory
import org.jetbrains.r.psi.RPsiUtil
import org.jetbrains.r.psi.RRecursiveElementVisitor
import org.jetbrains.r.psi.api.*
import org.jetbrains.r.psi.isFunctionFromLibrarySoft
import java.nio.file.Path
import javax.swing.Icon
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.appendText
import kotlin.io.path.forEachLine

@ExperimentalPathApi
class RS4StatAction : REditorActionBase {
  constructor() : super()

  constructor(text: String, description: String, icon: Icon?) : super(text, description, icon)

  private val root = Path.of("/", "home", "All_r_files")
  private val stat = root.resolve("stat")
  private val statRes = root.resolve("statRes")
  private val info = root.resolve("data_description_R.csv")

  override fun actionPerformed(e: AnActionEvent) {
    val data = listOf(
      StatDatas("setClass", 96825, SetClassAnalyser(statRes)),
      StatDatas("setGeneric", 285660, SetGenericAnalyser(statRes)),
      StatDatas("setMethod", 551957, SetMethodAnalyser(statRes))
    )

    val project = e.project
    runBackgroundableTask("S4 stat collection", e.project) { progressIndicator ->
      var i = 0
      val total = data.sumBy { it.count }
      for ((method, count, collector) in data) {
        var j = 0
        val visitor = RS4Visitor1(method, collector)
        val buffer = StringBuilder()
        var lines = 0
        fun run() {
          if (buffer.isBlank()) return
          val text = buffer.toString()
          runReadAction {
            i += 1
            j += 1
            progressIndicator.fraction = i.toDouble() / total
            progressIndicator.text = "$method: $j/$count"
            val rFile = RElementFactory.buildRFileFromText(project, text)
            visitor.visitElement(rFile)
          }
          buffer.clear()
          lines = 0
        }
        stat.resolve("$method.R").forEachLine {
          if (it.startsWith("# https://github.com")) run()
          buffer.append(it + "\n")
          lines += 1
        }

        if (buffer.isNotEmpty()) run()

        println("$method expr: ${visitor.count}")
        println("$method users: ${visitor.users.size}")
        println("$method repos: ${visitor.repos.size}")
        println(collector.getResults())
      }
    }
  }
}

private data class StatDatas(val method: String, val count: Int, val action: StatCollector)

private interface StatCollector {
  fun invoke(call: RCallExpression)
  fun getResults(): String
}

@ExperimentalPathApi
private class SetClassAnalyser(private val stat: Path) : StatCollector {

  var ubnormalTotal = 0
  val ubnormalSlots = mutableListOf<String>()
  var totalSlotsExprs = 0
  val totalSlotsRepos = mutableSetOf<String>()
  val totalSlotsUsers = mutableSetOf<String>()
  val ubnormalContains = mutableListOf<String>()
  var totalContainsExprs = 0
  val totalContainsRepos = mutableSetOf<String>()
  val totalContainsUsers = mutableSetOf<String>()
  val ubnormalRepresentation = mutableListOf<String>()
  var totalRepresentationExprs = 0
  val totalRepresentationRepos = mutableSetOf<String>()
  val totalRepresentationUsers = mutableSetOf<String>()

  lateinit var setClassDefinition: RAssignmentStatement

  override fun invoke(call: RCallExpression) {
    if (!this::setClassDefinition.isInitialized) {
      setClassDefinition = RPsiUtil.resolveCall(call).first()
    }
    val argumentInfo = RArgumentInfo.getArgumentInfo(call, setClassDefinition)!!

    val link = call.prevSibling.prevSibling.text
    val className = RS4ClassInfoUtil.getAssociatedClassName(call, argumentInfo)!!
    val representationArgument = argumentInfo.getArgumentPassedToParameter("representation")
    val slotsArgument = argumentInfo.getArgumentPassedToParameter("slots")
    val containsArgument = argumentInfo.getArgumentPassedToParameter("contains")

    val (user, repo) = getUserAndRepo(link)
    if (representationArgument != null) {
      totalRepresentationExprs += 1
      totalRepresentationRepos.add(repo)
      totalRepresentationUsers.add(user)
    }

    if (slotsArgument != null) {
      totalSlotsExprs += 1
      totalSlotsRepos.add(repo)
      totalSlotsUsers.add(user)
    }

    if (containsArgument != null) {
      totalContainsExprs += 1
      totalContainsRepos.add(repo)
      totalContainsUsers.add(user)
    }

    val isReprUbnormal = ubnormalRepresentationArgument(className, representationArgument)
    val isSlotsUbnormal = ubnormalSlotsArgument(className, slotsArgument)
    val isContainsUbnormal = ubnormalContainsArgument(containsArgument)

    fun writeTo(path: String, value: String) {
      val f = stat.resolve("$path.R")
      if (!f.exists()) f.createFile()
      f.appendText(value + "\n\n")
    }

    if (isReprUbnormal) {
      ubnormalRepresentation.add(link)
      writeTo("representation", representationArgument!!.text)
    }
    if (isSlotsUbnormal) {
      ubnormalSlots.add(link)
      writeTo("slots", slotsArgument!!.text)
    }
    if (isContainsUbnormal) {
      ubnormalContains.add(link)
      writeTo("contains", containsArgument!!.text)
    }
    if (isReprUbnormal || isSlotsUbnormal || isContainsUbnormal) {
      ubnormalTotal += 1
    }
  }

  override fun getResults(): String {
    val (reprUsers, repsReps) = ubnormalRepresentation.map { getUserAndRepo(it) }.unzip().let { it.first.toSet() to it.second.toSet() }
    val (slotsUsers, slotsReps) = ubnormalSlots.map { getUserAndRepo(it) }.unzip().let { it.first.toSet() to it.second.toSet() }
    val (containsUsers, containsReps) = ubnormalContains.map { getUserAndRepo(it) }.unzip().let { it.first.toSet() to it.second.toSet() }

    val totalUsers = reprUsers + slotsUsers + containsUsers
    val totalReps = repsReps + slotsReps + containsReps

    val printLinks = 0

    return """
      Total bad expr: $ubnormalTotal
      Total representations: $totalRepresentationExprs
      Bad representations: ${ubnormalRepresentation.size}
      ${ubnormalRepresentation.take(printLinks).joinToString("\n      ")}
      Total slots: $totalSlotsExprs
      Bad slots: ${ubnormalSlots.size}
      ${ubnormalSlots.take(printLinks).joinToString("\n      ")}
      Total contains: $totalContainsExprs
      Bad contains: ${ubnormalContains.size}
      ${ubnormalContains.take(printLinks).joinToString("\n      ")}
      
      Total bad repositories: ${totalReps.size}
      Total representations: ${totalRepresentationRepos.size}
      Representations repos: ${repsReps.size}
      ${repsReps.take(printLinks).joinToString("\n      ")}
      Total slots: ${totalSlotsRepos.size}
      Slots repos: ${slotsReps.size}
      ${slotsReps.take(printLinks).joinToString("\n      ")}
      Total contains: ${totalContainsRepos.size}
      Contains repos: ${containsReps.size}
      ${containsReps.take(printLinks).joinToString("\n      ")}
      
      Total users: ${totalUsers.size}
      Total representations: ${totalRepresentationUsers.size}
      Representation users: ${reprUsers.size}
      ${reprUsers.take(printLinks).joinToString("\n      ")}
      Total slots: ${totalSlotsUsers.size}
      Slots users: ${slotsUsers.size}
      ${slotsUsers.take(printLinks).joinToString("\n      ")}
      Total contains: ${totalContainsUsers.size}
      Contains users: ${containsUsers.size}
      ${containsUsers.take(printLinks).joinToString("\n      ")}
    """.trimIndent()
  }

  private fun ubnormalSlotsArgument(className: String, expr: RExpression?): Boolean {
    if (expr == null || expr is REmptyExpression) return false
    val argumentList = parseCharacterVector(expr) ?: return true
    argumentList.forEach { arg ->
      when (arg) {
        is RNamedArgument -> arg.toComplexSlots(className) ?: return true
        is RStringLiteralExpression -> {}
        is REmptyExpression -> {}
        else -> {
          // Most likely something strange and difficult to analyse statically
          return true
        }
      }
    }
    return false
  }

  private fun ubnormalContainsArgument(expr: RExpression?): Boolean {
    if (expr == null || expr is REmptyExpression) return false

    // Also `class representation` objects vector is suitable.
    // This is some rare use case which difficult to analyse statically
    val argumentList = parseCharacterVector(expr) ?: return true
    argumentList.forEach { arg ->
      when (arg) {
        is RNamedArgument -> if (arg.assignedValue !is RStringLiteralExpression) return true
        is RStringLiteralExpression -> { }
        is REmptyExpression -> {}
        else -> {
          // Most likely something strange and difficult to analyse statically
          return true
        }
      }
    }
    return false
  }

  private fun ubnormalRepresentationArgument(className: String, expr: RExpression?): Boolean {
    if (expr == null || expr is REmptyExpression) return false

    val argumentList = when (expr) {
      is RCallExpression -> {
        // Any function that returns a `list` is suitable.
        // Arbitrary function is some rare use case which difficult to analyse statically
        if (expr.isFunctionFromLibrarySoft("representation", "methods") ||
            expr.isFunctionFromLibrarySoft("list", "base") ||
            expr.isFunctionFromLibrarySoft("c", "base")) {
          expr.argumentList.expressionList
        }
        else return true
      }
      is RStringLiteralExpression -> listOf(expr)
      else -> return true
    }

    argumentList.forEach { arg ->
      when (arg) {
        is RNamedArgument -> arg.toSlot(className) ?: return true
        is RStringLiteralExpression -> { }
        is REmptyExpression -> {}
        else -> {
          // Most likely something strange and difficult to analyse statically
          return true
        }
      }
    }
    return false
  }

  private fun parseCharacterVector(expr: RExpression): List<RExpression>? = when (expr) {
    is RCallExpression -> {
      // Any function that returns a `vector` of `characters` is suitable.
      // Arbitrary function returns vector is some rare use case which difficult to analyse statically
      if (expr.isFunctionFromLibrarySoft("c", "base") ||
          expr.isFunctionFromLibrarySoft("list", "base") ||
          expr.isFunctionFromLibrarySoft("representation", "methods") ||
          expr.isFunctionFromLibrarySoft("signature", "methods")) {
        expr.argumentList.expressionList
      }
      else if (expr.isFunctionFromLibrarySoft("character", "base") &&
               expr.argumentList.text.let { it == "()" || it == "(0)" }) {
        emptyList()
      }
      else null
    }
    is RStringLiteralExpression -> listOf(expr)
    else -> null
  }

  private fun RNamedArgument.toComplexSlots(className: String): List<RS4ClassSlot>? {
    val namePrefix = name.takeIf { it.isNotEmpty() } ?: return null
    val types = assignedValue?.let { parseCharacterVector(it) } ?: return null
    var ind = 0
    return types.map {
      ind += 1
      when (it) {
        is RNamedArgument -> {
          val suffix = it.name
          val type = it.assignedValue?.toType() ?: return null
          RS4ClassSlot("$namePrefix.$suffix", type, className)
        }
        else -> {
          val type = it.toType() ?: return null
          val name = if (types.size == 1) namePrefix else "$namePrefix$ind"
          RS4ClassSlot(name, type, className)
        }
      }
    }
  }

  private fun RNamedArgument.toSlot(className: String): RS4ClassSlot? {
    val name = name.takeIf { it.isNotEmpty() } ?: return null
    val type = when (val typeExpr = assignedValue) {
                 is RCallExpression -> parseCharacterVector(typeExpr)?.firstOrNull()?.toType()
                 else -> typeExpr?.toType()
               } ?: return null
    return RS4ClassSlot(name, type, className)
  }

  private fun RPsiElement.toType(): String? {
    return when (this) {
      is RStringLiteralExpression -> name
      is RNaLiteral -> "NA"
      is RNullLiteral -> "NULL"
      else -> null
    }
  }
}

@ExperimentalPathApi
private class SetGenericAnalyser(root: Path) : StatCollector {

  private val stat = root.resolve("statRes")

  var ubnormalTotal = 0
  val ubnormalSignature = mutableListOf<String>()
  var totalSignatureExprs = 0
  val totalSignatureRepos = mutableSetOf<String>()
  val totalSignatureUsers = mutableSetOf<String>()
  val ubnormalValueClass = mutableListOf<String>()
  var totalValueClassExprs = 0
  val totalValueClassRepos = mutableSetOf<String>()
  val totalValueClassUsers = mutableSetOf<String>()

  var notStrName = 0
  var notStrIdentName = 0
  var notFuncDef = 0
  var notFuncIdentDef = 0

  lateinit var setGenericDefinition: RAssignmentStatement

  override fun invoke(call: RCallExpression) {
    if (!this::setGenericDefinition.isInitialized) {
      setGenericDefinition = RPsiUtil.resolveCall(call).first()
    }
    val argumentInfo = RArgumentInfo.getArgumentInfo(call, setGenericDefinition)!!

    val link = call.prevSibling.prevSibling.text
    val genericName = argumentInfo.getArgumentPassedToParameter("name")
    if (genericName != null && genericName !is RStringLiteralExpression) {
      ++notStrName
      if (genericName !is RIdentifierExpression) ++notStrIdentName
    }
    val def = argumentInfo.getArgumentPassedToParameter("def")
    if (def != null && def !is RFunctionExpression) {
      ++notFuncDef
      if (def !is RIdentifierExpression) ++notFuncIdentDef
    }
    val signatureArgument = argumentInfo.getArgumentPassedToParameter("signature")
    val valueClassArgument = argumentInfo.getArgumentPassedToParameter("valueClass")

    val (user, repo) = getUserAndRepo(link)
    if (signatureArgument != null) {
      totalSignatureExprs += 1
      totalSignatureRepos.add(repo)
      totalSignatureUsers.add(user)
    }

    if (valueClassArgument != null) {
      totalValueClassExprs += 1
      totalValueClassRepos.add(repo)
      totalValueClassUsers.add(user)
    }

    val isSignatureUbnormal = ubnormalCharacterVector(signatureArgument)
    val isValueClassUbnormal = ubnormalCharacterVector(valueClassArgument)

    fun writeTo(path: String, value: String) {
      val f = stat.resolve("$path.R")
      if (!f.exists()) f.createFile()
      f.appendText(value + "\n\n")
    }

    if (isSignatureUbnormal) {
      ubnormalSignature.add(link)
      writeTo("signature", signatureArgument!!.text)
    }
    if (isValueClassUbnormal) {
      ubnormalValueClass.add(link)
      writeTo("valueClass", valueClassArgument!!.text)
    }
    if (isSignatureUbnormal || isValueClassUbnormal) {
      ubnormalTotal += 1
    }
  }

  override fun getResults(): String {
    val (signatureUsers, signatureReps) = ubnormalSignature.map { getUserAndRepo(it) }.unzip().let { it.first.toSet() to it.second.toSet() }
    val (valueClassUsers, valueClassReps) = ubnormalValueClass.map { getUserAndRepo(it) }.unzip().let { it.first.toSet() to it.second.toSet() }

    val totalUsers = signatureUsers + valueClassUsers
    val totalReps = signatureReps + valueClassReps

    val printLinks = 0

    return """
      Not str name: $notStrName
      Not str or ident name: $notStrIdentName
      Not str def: $notFuncDef
      Not str or ident def: $notFuncIdentDef
      
      Total bad expr: $ubnormalTotal
      Total signature: $totalSignatureExprs
      Bad signature: ${ubnormalSignature.size}
      ${ubnormalSignature.take(printLinks).joinToString("\n      ")}
      Total valueClass: $totalValueClassExprs
      Bad valueClass: ${ubnormalValueClass.size}
      ${ubnormalValueClass.take(printLinks).joinToString("\n      ")}
      
      Total bad repositories: ${totalReps.size}
      Total signature: ${totalSignatureRepos.size}
      Bad signature repos: ${signatureReps.size}
      ${signatureReps.take(printLinks).joinToString("\n      ")}
      Total valueClass: ${totalValueClassRepos.size}
      Bad valueClass repos: ${valueClassReps.size}
      ${valueClassReps.take(printLinks).joinToString("\n      ")}
      
      Total users: ${totalUsers.size}
      Total signature: ${totalSignatureUsers.size}
      Bad signature users: ${signatureUsers.size}
      ${signatureUsers.take(printLinks).joinToString("\n      ")}
      Total valueClass: ${totalValueClassUsers.size}
      Bad valueClass users: ${valueClassUsers.size}
      ${valueClassUsers.take(printLinks).joinToString("\n      ")}
    """.trimIndent()
  }

  private fun ubnormalCharacterVector(expr: RExpression?): Boolean = when (expr) {
    null -> false
    is RCallExpression -> {
      // Any function that returns a `vector` of `characters` is suitable.
      // Arbitrary function returns vector is some rare use case which difficult to analyse statically
      if (expr.isFunctionFromLibrarySoft("c", "base") ||
          expr.isFunctionFromLibrarySoft("list", "base") ||
          expr.isFunctionFromLibrarySoft("representation", "methods") ||
          expr.isFunctionFromLibrarySoft("signature", "methods")) {
        expr.argumentList.expressionList.any { it !is RStringLiteralExpression && (it !is RNamedArgument || it.assignedValue !is RStringLiteralExpression) }
      }
      else if (expr.isFunctionFromLibrarySoft("character", "base") &&
               expr.argumentList.text.let { it == "()" || it == "(0)" }) {
        false
      }
      else true
    }
    is RStringLiteralExpression -> false
    else -> true
  }
}

@ExperimentalPathApi
private class SetMethodAnalyser(root: Path) : StatCollector {

  private val stat = root.resolve("statRes")

  var ubnormalTotal = 0
  val ubnormalSignature = mutableListOf<String>()
  var totalSignatureExprs = 0
  val totalSignatureRepos = mutableSetOf<String>()
  val totalSignatureUsers = mutableSetOf<String>()

  var notStrIdentName = 0
  var notFuncDef = 0
  var notFuncIdentDef = 0

  lateinit var setMethodDefinition: RAssignmentStatement

  override fun invoke(call: RCallExpression) {
    if (!this::setMethodDefinition.isInitialized) {
      setMethodDefinition = RPsiUtil.resolveCall(call).first()
    }
    val argumentInfo = RArgumentInfo.getArgumentInfo(call, setMethodDefinition)!!

    val link = call.prevSibling.prevSibling.text
    val genericName = argumentInfo.getArgumentPassedToParameter("f")
    if (genericName != null && genericName !is RStringLiteralExpression && genericName !is RIdentifierExpression) {
      ++notStrIdentName
    }
    val def = argumentInfo.getArgumentPassedToParameter("definition")
    if (def != null && def !is RFunctionExpression) {
      ++notFuncDef
      if (def !is RIdentifierExpression) ++notFuncIdentDef
    }
    val signatureArgument = argumentInfo.getArgumentPassedToParameter("signature")

    val (user, repo) = getUserAndRepo(link)
    if (signatureArgument != null) {
      totalSignatureExprs += 1
      totalSignatureRepos.add(repo)
      totalSignatureUsers.add(user)
    }

    val isSignatureUbnormal = ubnormalCharacterVector(signatureArgument)

    fun writeTo(path: String, value: String) {
      val f = stat.resolve("$path.R")
      if (!f.exists()) f.createFile()
      f.appendText(value + "\n\n")
    }

    if (isSignatureUbnormal) {
      ubnormalTotal += 1
      ubnormalSignature.add(link)
      writeTo("signature_method", signatureArgument!!.text)
    }
  }

  override fun getResults(): String {
    val (signatureUsers, signatureReps) = ubnormalSignature.map { getUserAndRepo(it) }.unzip().let { it.first.toSet() to it.second.toSet() }

    val totalUsers = signatureUsers
    val totalReps = signatureReps

    val printLinks = 0

    return """
      Not str or ident name: $notStrIdentName
      Not str def: $notFuncDef
      Not str or ident def: $notFuncIdentDef
      
      Total bad expr: $ubnormalTotal
      Total signature: $totalSignatureExprs
      Bad signature: ${ubnormalSignature.size}
      ${ubnormalSignature.take(printLinks).joinToString("\n      ")}
      
      Total bad repositories: ${totalReps.size}
      Total signature: ${totalSignatureRepos.size}
      Bad signature repos: ${signatureReps.size}
      ${signatureReps.take(printLinks).joinToString("\n      ")}
      
      Total users: ${totalUsers.size}
      Total signature: ${totalSignatureUsers.size}
      Bad signature users: ${signatureUsers.size}
      ${signatureUsers.take(printLinks).joinToString("\n      ")}
    """.trimIndent()
  }

  private fun ubnormalCharacterVector(expr: RExpression?): Boolean = when (expr) {
    null -> false
    is RCallExpression -> {
      // Any function that returns a `vector` of `characters` is suitable.
      // Arbitrary function returns vector is some rare use case which difficult to analyse statically
      if (expr.isFunctionFromLibrarySoft("c", "base") ||
          expr.isFunctionFromLibrarySoft("list", "base") ||
          expr.isFunctionFromLibrarySoft("representation", "methods") ||
          expr.isFunctionFromLibrarySoft("signature", "methods")) {
        expr.argumentList.expressionList.any { it !is RStringLiteralExpression && (it !is RNamedArgument || it.assignedValue !is RStringLiteralExpression) }
      }
      else if (expr.isFunctionFromLibrarySoft("character", "base") &&
               expr.argumentList.text.let { it == "()" || it == "(0)" }) {
        false
      }
      else true
    }
    is RStringLiteralExpression -> false
    else -> true
  }
}


private class RS4Visitor1(val functionName: String, val action: StatCollector) : RRecursiveElementVisitor() {
  var count = 0
  var repos = mutableSetOf<String>()
  var users = mutableSetOf<String>()

  override fun visitCallExpression(o: RCallExpression) {
    if (o.isFunctionFromLibrarySoft(functionName, "methods")) {
      action.invoke(o)
      count += 1
      val (user, repo) = getUserAndRepo(o.prevSibling.prevSibling.text)
      users.add(user)
      repos.add(repo)
      val old = count
      //o.acceptChildren(this)
      if (count > old && o.text.lines().count { it.startsWith(functionName) } > 1) {
        println(o.prevSibling.prevSibling.text)
      }
    }
  }
}

private fun getUserAndRepo(link: String): Pair<String, String> {
  val (user, repo) = link.drop("# https://github.com/".length).split("/").take(2)
  return user to "$user/$repo"
}