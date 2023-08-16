// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.intellij.samples.psi;

import com.intellij.lang.jvm.JvmParameter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * PSI演示项目通过实现“AnAction”来演示PSI导航的工作，通过消息对话框通知:
 * - 插入符号处的元素，
 * -包含方法，
 * -包含类，
 * -局部变量
 */
public class PsiNavigationDemoAction extends AnAction {

//  @Override
//  public @NotNull ActionUpdateThread getActionUpdateThread() {
//    return ActionUpdateThread.BGT;
//  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
    PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
    if (editor == null || psiFile == null) {
      return;
    }
    int offset = editor.getCaretModel().getOffset();

    final StringBuilder infoBuilder = new StringBuilder();
    //根据光标位置，查找当前PSI元素
    PsiElement element = psiFile.findElementAt(offset);
    infoBuilder.append("Element at caret: [").append(element).append("]\n");
    if (element != null) {
      //元素非空的话，查找其在哪个方法中
      PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
      boolean findParentMethod = containingMethod != null;
      infoBuilder.append("Containing method: ");
      if(findParentMethod){
        JvmParameter[] parameters = containingMethod.getParameters();
        infoBuilder.append(containingMethod.getName());
        if(parameters.length > 0){
          infoBuilder.append("\n");
          infoBuilder.append("paramters size:").append(parameters.length);
        }
      }else{
        infoBuilder.append("none");
      }
      infoBuilder.append("\n");

      if (findParentMethod) {
        PsiClass containingClass = containingMethod.getContainingClass();
        infoBuilder
                .append("Containing class: ")
                .append(containingClass != null ? containingClass.getName() : "none")
                .append("\n");

        infoBuilder.append("Local variables:\n");
        //查找方法中的局部变量
        containingMethod.accept(new JavaRecursiveElementVisitor() {
          @Override
          public void visitLocalVariable(@NotNull PsiLocalVariable variable) {
            super.visitLocalVariable(variable);
            infoBuilder.append("  - ").append(variable.getName()).append("\n");
          }
        });
      }
    }
    Messages.showMessageDialog(anActionEvent.getProject(), infoBuilder.toString(), "PSI Info", null);
  }

  @Override
  public void update(AnActionEvent e) {
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    e.getPresentation().setEnabled(editor != null && psiFile != null);
  }

}
