package org.intellij.samples.hightlight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil


/**
 * 高亮功能的Demo过滤器
 */
class HelloWorldHightLightFilter : HighlightInfoFilter {
    override fun accept(info: HighlightInfo, file: PsiFile?): Boolean {
        if (null == file) {
            return false
        }
        val psiElement:PsiElement = file.findElementAt(info.getStartOffset()) ?: return true
        val containingMethod = PsiTreeUtil.getParentOfType<PsiMethod>(
            psiElement,
            PsiMethod::class.java
        ) ?: return false
        val containingClass = containingMethod.containingClass
//        val text = info.text
//        val severity = info.severity
//        val description = info.description
//        val range = info.range
//        val substring = range.substring(file.text)
        return true
    }
}