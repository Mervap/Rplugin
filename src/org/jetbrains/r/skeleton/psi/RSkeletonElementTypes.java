/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.skeleton.psi;

import org.jetbrains.r.classes.s4.extra.stubs.RSkeletonCallExpressionElementType;

public interface RSkeletonElementTypes {
  RSkeletonAssignmentElementType R_SKELETON_ASSIGNMENT_STATEMENT = new RSkeletonAssignmentElementType();
  RSkeletonCallExpressionElementType R_SKELETON_CALL_EXPRESSION = new RSkeletonCallExpressionElementType();
}
