// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.intellij.samples.psi;

import com.intellij.lang.jvm.JvmParameter;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * PSI导航分为3种方式：
 * - 从上向下：
 *    当需要查询PSI文件/元素的 子PSI元素时（例如：查找它某个方法包含的变量）
 *    从上向下导航常用Visitor <p>
 *    大多数情况下，也可以使用特定的API进行导航。如 PsiClass.getMethods() 来获取一个java class的所有方法。<p>
 *    并且PsiTreeUtil 提供了很多通用的，跟编程语言无关的方法，例如 findChildrenOfType(
 *    <p>
 * - 从下向上：
 *    当需要查找某一个PSI元素的父PSI元素时（例如：查找某个变量所在的方法）。
 *    大多数情况下，使用 PsiTreeUtil.getParentOfType() 方法进行自下而上的查找
 * <p>
 * - 引用：
 *   如果一个PSI元素是引用对象，可以导航到它的本体（声明该对象的地方），也可以从本体查找所有引用它的PSI元素。
 * （Ctrl+鼠标左键的跳转功能）
 *
 * 这个demo主要演示“从上向下”和“从下向上”的导航方式。
 */
public class PsiNavigationTopAndBottomAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) {
            return;
        }
        final StringBuilder infoBuilder = new StringBuilder();
        // 获取 PsiFile 的名称，包括扩展名
        infoBuilder.append("所属文件: ").append(psiFile.getName()).append("\n");

        // 1. 获取偏移量
        int offset = editor.getCaretModel().getOffset();
        // 2. 查找到PSI元素
        PsiElement element = psiFile.findElementAt(offset);

        infoBuilder.append("插入符所在元素: [").append(element).append("]\n");
        // 3. 访问它的前后关系，可以使用PSI树进行查找
        if (element != null) {
            boolean isInMethod = navigationFromTopToBottom(element, infoBuilder);
            if(!isInMethod){
                navigationFromBottomToTop(element, infoBuilder);
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


    /**
     * 【自上而下导航】
     * @param element
     * @param infoBuilder
     * @return 当前的PsiElement是否在方法中
     */
    private boolean navigationFromTopToBottom(PsiElement element,StringBuilder infoBuilder){
        //通过PsiTreeUtil.getParentOfType()方法，从PSI自身开始访问它的所有父元素，直到找到目标元素类型即返回。
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        infoBuilder.append("所在方法: ");
        if(containingMethod == null) {
            infoBuilder.append("none").append("\n");
            return false;
        }
        infoBuilder.append(containingMethod.getName());
        collectMethodParamsType(containingMethod, infoBuilder);
        // 获取PsiMethod对象的参数列表
        JvmParameter[] parameters = containingMethod.getParameters();
        if (parameters.length > 0) {
            infoBuilder.append("参数个数:").append(parameters.length).append("\n");
        }
        //有父级方法存在的话，查找方法所在的类
        PsiClass containingClass = containingMethod.getContainingClass();
        infoBuilder.append("所在类: ")
                .append(containingClass != null ? containingClass.getName() : "none")
                .append("\n");

        //查找方法中的局部变量
        infoBuilder.append("方法本地变量:\n");
        containingMethod.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitLocalVariable(@NotNull PsiLocalVariable variable) {
                super.visitLocalVariable(variable);
                infoBuilder.append("  - ").append(variable.getName()).append("\n");
            }
        });
        infoBuilder.append("\n");
        return true;
    }


    /**
     * 如果PsiElement不在method中，就进行【自下而上】的导航
     *
     * @param element
     * @param infoBuilder
     */
    private void navigationFromBottomToTop(PsiElement element,StringBuilder infoBuilder) {
        //向上查询所在的类
        PsiClass containingClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        infoBuilder.append("所在类: ")
                .append(containingClass != null ? containingClass.getName() : "none")
                .append("\n");

        if(containingClass != null){
            infoBuilder.append("包含的Fields:\n");
            containingClass.accept(new JavaRecursiveElementVisitor() {
                /**
                 * 这个方法会对每一个变量的variable元素调用一次，包括全局变量、函数入参变量、函数本地变量
                 * @param variable
                 */
                @Override
                public void visitVariable(@NotNull PsiVariable variable) {
                    //variable.getText() PSI元素的文本 整行变量的代码文字
                    super.visitVariable(variable);
                    if(variable instanceof PsiField) {
                        infoBuilder.append("  - ")
                                .append(variable.getName())
                                .append("\n");
                    }
                }
            });

            infoBuilder.append("方法:\n");
            // 有父级的class元素，就查找当前class的所有方法
            PsiMethod[] methods = containingClass.getMethods();
            if(methods.length > 0){
                for (PsiMethod method : methods) {
                    infoBuilder.append(" - ") .append(method.getName());
                    collectMethodParamsType(method, infoBuilder);
                }
            }else{
                infoBuilder.append("none").append("\n");
            }
        }
    }

    /**
     * 模仿StructureView的导航, 展示方法的参数列表
     * @param method   PsiMethod
     * @param infoBuilder  StringBuilder
     */
    private void collectMethodParamsType(PsiMethod method, StringBuilder infoBuilder) {
        //获取方法的参数列表PSI对象
        PsiParameterList parameterList = method.getParameterList();
        PsiParameter[] parametersArray = parameterList.getParameters();
        infoBuilder.append("(");
        for (int i = 0; i < parametersArray.length; i++) {
            //依次遍历获取函数的参数类型
            PsiParameter  psiParameter = parametersArray[i];
            PsiTypeElement pType = PsiTreeUtil.findChildOfType(psiParameter, PsiTypeElement.class);
            if(pType == null){
                continue;
            }
            String typeText = pType.getText();
            infoBuilder.append(typeText);
            if(i != (parametersArray.length -1)){
                infoBuilder.append(", ");
            }
        }
        infoBuilder.append(")\n");
    }
}
