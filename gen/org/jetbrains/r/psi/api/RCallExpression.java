// This is a generated file. Not intended for manual editing.
package org.jetbrains.r.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.r.classes.s4.extra.RS4GenericOrMethodHolder;
import com.intellij.psi.StubBasedPsiElement;
import org.jetbrains.r.classes.s4.extra.stubs.RCallExpressionStub;
import org.jetbrains.r.classes.s4.classInfo.RS4ClassInfo;

public interface RCallExpression extends RExpression, RS4GenericOrMethodHolder, StubBasedPsiElement<RCallExpressionStub> {

  @NotNull
  RArgumentList getArgumentList();

  @NotNull
  RExpression getExpression();

  @Nullable
  RS4ClassInfo getAssociatedS4ClassInfo();

}
