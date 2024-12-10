// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.samples.psi

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

/**
 * PSI导航分为3种方式：
 * - 从上向下：
 * 当需要查询PSI文件/元素的 子PSI元素时（例如：查找它某个方法包含的变量）
 * 从上向下导航常用Visitor
 *
 *
 * 大多数情况下，也可以使用特定的API进行导航。如 PsiClass.getMethods() 来获取一个java class的所有方法。
 *
 *
 * 并且PsiTreeUtil 提供了很多通用的，跟编程语言无关的方法，例如 findChildrenOfType(
 *
 *
 * - 从下向上：
 * 当需要查找某一个PSI元素的父PSI元素时（例如：查找某个变量所在的方法）。
 * 大多数情况下，使用 PsiTreeUtil.getParentOfType() 方法进行自下而上的查找
 *
 *
 * - 引用：
 * 如果一个PSI元素是引用对象，可以导航到它的本体（声明该对象的地方），也可以从本体查找所有引用它的PSI元素。
 * （Ctrl+鼠标左键的跳转功能）
 *
 * 这个demo主要演示“从上向下”和“从下向上”的导航方式。
 */
class PsiNavigationTopAndBottomAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val editor = anActionEvent.getData(CommonDataKeys.EDITOR)
        val psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE)
        if (editor == null || psiFile == null) {
            return
        }
        val infoBuilder = StringBuilder()
        // 获取 PsiFile 的名称，包括扩展名
        infoBuilder.append("根据ActionEvent获取PsiFile文件名称: ").append(psiFile.name).append("\n")

        // 1. 获取偏移量
        val offset = editor.caretModel.offset
        // 2. 查找到PSI元素
        val element = psiFile.findElementAt(offset)
        infoBuilder.append("插入符所在元素: [").append(element).append("]\n")
        // 3. 访问它的前后关系，可以使用PSI树进行查找
        if (element != null) {
            // 因为这个AnActionEvent行为，是必须editor和psiFile都存在的情况下，才是enable的，所以不可能为null
            val psiContainingFileName = element.containingFile.name
            infoBuilder.append("根据插入符元素反查文件名称: ").append(psiContainingFileName).append("\n")
            val isInMethod = navigationFromTopToBottom(element, infoBuilder)
            if (!isInMethod) {
                navigationFromBottomToTop(element, infoBuilder)
            }
        }
        Messages.showMessageDialog(anActionEvent.project, infoBuilder.toString(), "PSI Info", null)
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabled = editor != null && psiFile != null
    }

    /**
     * 【自上而下导航】
     * @param element
     * @param infoBuilder
     * @return 当前的PsiElement是否在方法中
     */
    private fun navigationFromTopToBottom(element: PsiElement, infoBuilder: StringBuilder): Boolean {
        //通过PsiTreeUtil.getParentOfType()方法，从PSI自身开始访问它的所有父元素，直到找到目标元素类型即返回。
        val containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
        infoBuilder.append("所在方法: ")
        if (containingMethod == null) {
            infoBuilder.append("none").append("\n")
            return false
        }
        infoBuilder.append(containingMethod.name)
        collectMethodParamsType(containingMethod, infoBuilder)
        // 获取PsiMethod对象的参数列表
        val parameters = containingMethod.parameters
        if (parameters.isNotEmpty()) {
            infoBuilder.append("参数个数:").append(parameters.size).append("\n")
        }
        //有父级方法存在的话，查找方法所在的类
        val containingClass = containingMethod.containingClass
        infoBuilder.append("所在类: ")
            .append(if (containingClass != null) containingClass.name else "none")
            .append("\n")

        //查找方法中的局部变量
        infoBuilder.append("方法本地变量:\n")
        containingMethod.accept(object : JavaRecursiveElementVisitor() {
            override fun visitLocalVariable(variable: PsiLocalVariable) {
                super.visitLocalVariable(variable)
                infoBuilder.append("  - ").append(variable.name).append("\n")
            }
        })
        infoBuilder.append("\n")
        return true
    }

    /**
     * 如果PsiElement不在method中，就进行【自下而上】的导航
     *
     * @param element
     * @param infoBuilder
     */
    private fun navigationFromBottomToTop(element: PsiElement, infoBuilder: StringBuilder) {
        //向上查询所在的类
        val containingClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
        infoBuilder.append("所在类: ")
            .append(if (containingClass != null) containingClass.name else "none")
            .append("\n")
        if (containingClass != null) {
            infoBuilder.append("包含的Fields:\n")
            containingClass.accept(object : JavaRecursiveElementVisitor() {
                /**
                 * 这个方法会对每一个变量的variable元素调用一次，包括全局变量、函数入参变量、函数本地变量
                 * @param variable
                 */
                override fun visitVariable(variable: PsiVariable) {
                    //variable.getText() PSI元素的文本 整行变量的代码文字
                    super.visitVariable(variable)
                    if (variable is PsiField) {
                        infoBuilder.append("  - ")
                            .append(variable.name)
                            .append("\n")
                    }
                }
            })
            infoBuilder.append("方法:\n")
            // 有父级的class元素，就查找当前class的所有方法
            val methods = containingClass.methods
            if (methods.isNotEmpty()) {
                for (method in methods) {
                    infoBuilder.append(" - ").append(method.name)
                    collectMethodParamsType(method, infoBuilder)
                }
            } else {
                infoBuilder.append("none").append("\n")
            }
        }
    }

    /**
     * 模仿StructureView的导航, 展示方法的参数列表
     * @param method   PsiMethod
     * @param infoBuilder  StringBuilder
     */
    private fun collectMethodParamsType(method: PsiMethod, infoBuilder: StringBuilder) {
        //获取方法的参数列表PSI对象
        val parameterList = method.parameterList
        val parametersArray = parameterList.parameters
        infoBuilder.append("(")
        for (i in parametersArray.indices) {
            //依次遍历获取函数的参数类型
            val psiParameter = parametersArray[i]
            val pType = PsiTreeUtil.findChildOfType(psiParameter,PsiTypeElement::class.java)?: continue
            val typeText = pType.text
            infoBuilder.append(typeText)
            if (i != parametersArray.size - 1) {
                infoBuilder.append(", ")
            }
        }
        infoBuilder.append(")\n")
    }
}
