/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4.extra

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import org.jetbrains.r.classes.s4.RS4Resolver
import org.jetbrains.r.classes.s4.classInfo.RS4ClassInfo
import org.jetbrains.r.classes.s4.classInfo.RS4ClassInfoUtil
import org.jetbrains.r.classes.s4.classInfo.RS4ClassSlot
import org.jetbrains.r.classes.s4.context.RS4ContextProvider
import org.jetbrains.r.classes.s4.context.RS4NewObjectClassNameContext
import org.jetbrains.r.classes.s4.context.RS4NewObjectContext
import org.jetbrains.r.classes.s4.context.RS4NewObjectSlotNameContext
import org.jetbrains.r.classes.s4.context.setClass.RS4SetClassTypeUsageContext
import org.jetbrains.r.classes.s4.extra.index.RS4ClassNameIndex
import org.jetbrains.r.console.RConsoleRuntimeInfo
import org.jetbrains.r.console.RConsoleView
import org.jetbrains.r.console.runtimeInfo
import org.jetbrains.r.editor.completion.*
import org.jetbrains.r.psi.RPsiUtil
import org.jetbrains.r.psi.api.*
import org.jetbrains.r.psi.references.RSearchScopeUtil

class AtAccessCompletionProvider(private val rCompletionElementFactory: RLookupElementFactory) : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val file = parameters.originalFile
    val atAccess = PsiTreeUtil.getParentOfType(parameters.position, RAtExpression::class.java) ?: return
    addStaticRuntimeCompletionDependsOfFile(atAccess, file, result, AtAccessStaticRuntimeCompletionProvider())
  }

  private inner class AtAccessStaticRuntimeCompletionProvider : RStaticRuntimeCompletionProvider<RAtExpression> {
    override fun addCompletionFromRuntime(psiElement: RAtExpression,
                                          shownNames: MutableSet<String>,
                                          result: CompletionResultSet,
                                          runtimeInfo: RConsoleRuntimeInfo): Boolean {
      val obj = psiElement.leftExpr ?: return false
      // obj@<caret>
      // pck::obj@<caret>
      // env$obj@<caret>
      if (obj !is RIdentifierExpression &&
          obj !is RNamespaceAccessExpression &&
          (obj !is RMemberExpression || obj.rightExpr !is RIdentifierExpression)) {
        return false
      }
      val text = obj.text
      runtimeInfo.loadS4ClassInfoByObjectName(text)?.let { info ->
        return addSlotsCompletion(info.slots, shownNames, result)
      }
      return false
    }

    override fun addCompletionStatically(psiElement: RAtExpression,
                                         shownNames: MutableSet<String>,
                                         result: CompletionResultSet): Boolean {
      val owner = psiElement.leftExpr ?: return false
      var res = false
      RS4Resolver.findElementS4ClassDeclarations(owner).forEach {
        res = res || addSlotsCompletion(RS4ClassInfoUtil.getAllAssociatedSlots(it), shownNames, result)
      }
      return res
    }

    private fun addSlotsCompletion(slots: List<RS4ClassSlot>, shownNames: MutableSet<String>, result: CompletionResultSet): Boolean {
      var hasNewResults = false
      for (slot in slots) {
        if (slot.name in shownNames) continue
        result.consume(rCompletionElementFactory.createAtAccess(slot.name, slot.type))
        shownNames.add(slot.name)
        hasNewResults = true
      }
      return hasNewResults
    }
  }
}

class S4ClassContextCompletionProvider(private val rCompletionElementFactory: RLookupElementFactory)
  : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val expression = PsiTreeUtil.getParentOfType(parameters.position, RExpression::class.java, false) ?: return
    val file = parameters.originalFile
    addS4ClassNameCompletion(expression, file, result)
    addS4SlotNameCompletion(expression, file, result)
  }

  private fun addS4SlotNameCompletion(classNameExpression: RExpression, file: PsiFile, result: CompletionResultSet) {
    val s4Context = RS4ContextProvider.getS4Context(classNameExpression, RS4NewObjectContext::class) ?: return
    if (s4Context !is RS4NewObjectSlotNameContext) return

    val newCall = s4Context.contextFunctionCall
    val className = RS4ClassInfoUtil.getAssociatedClassName(newCall) ?: return
    addStaticRuntimeCompletionDependsOfFile(newCall, file, result,
      object : RStaticRuntimeCompletionProvider<RCallExpression> {
        override fun addCompletionFromRuntime(psiElement: RCallExpression,
                                              shownNames: MutableSet<String>,
                                              result: CompletionResultSet,
                                              runtimeInfo: RConsoleRuntimeInfo): Boolean {
          runtimeInfo.loadS4ClassInfoByClassName(className)?.let { info ->
            info.slots.forEach {
              result.consume(RLookupElementFactory.createNamedArgumentLookupElement(it.name, it.type, SLOT_NAME_PRIORITY))
            }
            return true
          }
          return false
        }

        override fun addCompletionStatically(psiElement: RCallExpression,
                                             shownNames: MutableSet<String>,
                                             result: CompletionResultSet): Boolean {
          RS4ClassNameIndex.findClassDefinitions(className,
                                                 psiElement.project,
                                                 RSearchScopeUtil.getScope(
                                                   psiElement)).singleOrNull()?.let { definition ->
            RS4ClassInfoUtil.getAllAssociatedSlots(definition).forEach {
              result.consume(RLookupElementFactory.createNamedArgumentLookupElement(it.name, it.type, SLOT_NAME_PRIORITY))
            }
            return true
          }
          return false
        }
      })
  }

  private fun addS4ClassNameCompletion(classNameExpression: RExpression,
                                       file: PsiFile,
                                       result: CompletionResultSet) {
    val s4Context = RS4ContextProvider.getS4Context(classNameExpression, *RS4ContextProvider.S4_CLASS_USAGE_CONTEXTS) ?: return
    var omitVirtual = false
    var nameToOmit: String? = null
    when (s4Context) {
      is RS4NewObjectClassNameContext -> {
        omitVirtual = true
      }
      is RS4SetClassTypeUsageContext -> {
        nameToOmit = RS4ClassInfoUtil.getAssociatedClassName(s4Context.contextFunctionCall)
      }
    }

    val project = classNameExpression.project
    val scope = RSearchScopeUtil.getScope(classNameExpression)
    val runtimeInfo = file.runtimeInfo
    val loadedPackages = runtimeInfo?.loadedPackages?.keys
    val shownNames = HashSet<String>()
    RS4ClassNameIndex.processAllS4ClassInfos(project, scope, Processor { (declaration, info) ->
      if (omitVirtual && info.isVirtual) return@Processor true
      if (nameToOmit != info.className) {
        result.addS4ClassName(classNameExpression, declaration, info, shownNames, loadedPackages)
      }
      return@Processor true
    })
    runtimeInfo?.loadShortS4ClassInfos()?.forEach { info ->
      if (omitVirtual && info.isVirtual) return@forEach
      if (nameToOmit != info.className) {
        result.addS4ClassName(classNameExpression, null, info, shownNames, loadedPackages)
      }
    }
  }

  private fun CompletionResultSet.addS4ClassName(classNameExpression: RExpression,
                                                 classDeclaration: RCallExpression?,
                                                 classInfo: RS4ClassInfo,
                                                 shownNames: MutableSet<String>,
                                                 loadedPackages: Set<String>?) {
    val className = classInfo.className
    if (className in shownNames) return
    shownNames.add(className)

    val packageName = classInfo.packageName
    val isUser = classDeclaration != null && !RPsiUtil.isLibraryElement(classDeclaration)
    val isLoaded = loadedPackages?.contains(packageName) ?: true
    val priority =
      when {
        classInfo.packageName == "methods" && classInfo.superClasses.any { it.name == "language" } -> LANGUAGE_S4_CLASS_NAME
        isUser || isLoaded -> LOADED_S4_CLASS_NAME
        else -> NOT_LOADED_S4_CLASS_NAME
      }
    val location =
      if (isUser) {
        val virtualFile = classDeclaration!!.containingFile.virtualFile
        val projectDir = classDeclaration.project.guessProjectDir()
        if (virtualFile == null || projectDir == null) ""
        else VfsUtil.getRelativePath(virtualFile, projectDir) ?: ""
      } else packageName
    if (classNameExpression is RStringLiteralExpression) {
      addElement(RLookupElementFactory.createLookupElementWithPriority(
        RLookupElement(escape(className), true, AllIcons.Nodes.Field, packageName = location),
        STRING_LITERAL_INSERT_HANDLER, priority))
    } else {
      addElement(rCompletionElementFactory.createQuotedLookupElement(className, priority, true, AllIcons.Nodes.Field,
                                                                     location))
    }
  }

  private val escape = StringUtil.escaper(true, "\"")::`fun`
  private val STRING_LITERAL_INSERT_HANDLER = InsertHandler<LookupElement> { insertHandlerContext, _ ->
    insertHandlerContext.file.findElementAt(insertHandlerContext.editor.caretModel.offset)?.let { element ->
      insertHandlerContext.editor.caretModel.moveToOffset(element.textRange.endOffset)
    }
  }
}

/**
 * If the [file] is a console, it searches for results first in runtime. Then statically, if no results have been found.
 * Otherwise in a different order
 * @see [RStaticRuntimeCompletionProvider]
 */
private fun <T : PsiElement> addStaticRuntimeCompletionDependsOfFile(psiElement: T,
                                                                     file: PsiFile,
                                                                     result: CompletionResultSet,
                                                                     provider: RStaticRuntimeCompletionProvider<T>) {
  val runtimeInfo = file.runtimeInfo
  val shownNames = HashSet<String>()
  if (file.getUserData(RConsoleView.IS_R_CONSOLE_KEY) == true) {
    if (runtimeInfo == null || !provider.addCompletionFromRuntime(psiElement, shownNames, result, runtimeInfo)) {
      provider.addCompletionStatically(psiElement, shownNames, result)
    }
  } else {
    if (!provider.addCompletionStatically(psiElement, shownNames, result)) {
      runtimeInfo?.let { provider.addCompletionFromRuntime(psiElement, shownNames, result, it) }
    }
  }
}

private interface RStaticRuntimeCompletionProvider<T : PsiElement> {

  /**
   * @return true if the required lookup elements have already been found. False otherwise
   */
  fun addCompletionFromRuntime(psiElement: T,
                               shownNames: MutableSet<String>,
                               result: CompletionResultSet,
                               runtimeInfo: RConsoleRuntimeInfo): Boolean

  /**
   * @return true if the required lookup elements have already been found. False otherwise
   */
  fun addCompletionStatically(psiElement: T,
                              shownNames: MutableSet<String>,
                              result: CompletionResultSet): Boolean
}

