/*
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.classes.s4

import com.intellij.pom.PomDescriptionProvider
import com.intellij.pom.PomTarget
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewNodeTextLocation
import com.intellij.usageView.UsageViewShortNameLocation
import com.intellij.usageView.UsageViewTypeLocation
import org.jetbrains.r.RBundle
import org.jetbrains.r.classes.s4.classInfo.RS4ComplexSlotPomTarget
import org.jetbrains.r.classes.s4.classInfo.RSkeletonS4ClassPomTarget
import org.jetbrains.r.classes.s4.classInfo.RSkeletonS4SlotPomTarget
import org.jetbrains.r.classes.s4.classInfo.RStringLiteralPomTarget
import org.jetbrains.r.classes.s4.context.RS4ContextProvider
import org.jetbrains.r.classes.s4.context.setClass.RS4SetClassClassNameContext

class RS4PomTargetDescriptionProvider : PomDescriptionProvider() {
  override fun getElementDescription(element: PomTarget, location: ElementDescriptionLocation): String? = when (location) {
    UsageViewTypeLocation.INSTANCE -> getType(element)
    UsageViewShortNameLocation.INSTANCE -> getShortName(element)
    UsageViewNodeTextLocation.INSTANCE -> getType(element) + " " + getShortName(element)
    UsageViewLongNameLocation.INSTANCE -> getLongName(element)
    else -> null
  }

  private fun getType(element: PomTarget): String? = when (element) {
    is RSkeletonS4ClassPomTarget -> RBundle.message("find.usages.s4.class")
    is RSkeletonS4SlotPomTarget, is RS4ComplexSlotPomTarget -> RBundle.message("find.usages.s4.slot")
    is RStringLiteralPomTarget -> {
      if (RS4ContextProvider.getS4Context(element.literal, RS4SetClassClassNameContext::class) != null) {
        RBundle.message("find.usages.s4.class")
      }
      else null
    }
    else -> null
  }

  private fun getShortName(element: PomTarget): String? = when (element) {
    is RSkeletonS4ClassPomTarget -> element.setClass.associatedS4ClassInfo.className
    is RSkeletonS4SlotPomTarget -> element.name
    is RS4ComplexSlotPomTarget -> element.slot.name
    is RStringLiteralPomTarget -> element.literal.name
    else -> null
  }

  private fun getLongName(element: PomTarget): String? = when (element) {
    is RSkeletonS4ClassPomTarget -> element.setClass.associatedS4ClassInfo.let { "${it.packageName}::setClass('${it.className}')" }
    is RSkeletonS4SlotPomTarget -> element.slotInfo?.let { "${it.name} = '${it.type}'" }
    is RS4ComplexSlotPomTarget -> element.slot.let { "${it.name} = '${it.type}'" }
    else -> null
  }
}