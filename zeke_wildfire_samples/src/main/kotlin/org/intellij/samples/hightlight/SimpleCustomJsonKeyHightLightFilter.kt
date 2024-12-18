package org.intellij.samples.hightlight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter
import com.intellij.json.JsonElementTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement


/**
 *
 * 简单的自定义Json文件key高亮功能的Demo
 */
class SimpleCustomJsonKeyHightLightFilter : BaseHighlightInfoFilter() {

    override fun filterHighlightInfo(highlightInfo: HighlightInfo, file: PsiFile): Boolean {
        val psiElement:PsiElement = file.findElementAt(highlightInfo.getStartOffset()) ?: return true
        if(psiElement !is LeafPsiElement){
            return false // 元素类型不是LeafPsiElement，则不做处理，不拦截
        }
        return isFilterHighlightInfo(psiElement)
    }

    private fun isFilterHighlightInfo(psiElement:PsiElement):Boolean{
        val content = psiElement.text
        val substring = content.substring(1, content.length - 1)
        return substring == "DisableHightlight"
    }
}