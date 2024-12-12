package org.intellij.samples.hightlight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter
import com.intellij.json.JsonElementTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement


/**
 * 简单的自定义Json文件key高亮功能的Demo
 *
 */
class SimpleCustomJsonKeyHightLightFilter : HighlightInfoFilter {
    override fun accept(info: HighlightInfo, file: PsiFile?): Boolean {
        if (null == file) {
            return false
        }
        val psiElement:PsiElement = file.findElementAt(info.getStartOffset()) ?: return true
        if(psiElement !is LeafPsiElement){
            return false
        }
        if(psiElement.elementType.toString() != JsonElementTypes.DOUBLE_QUOTED_STRING.toString()){
            return false
        }
        return isNeedHightLight(psiElement)
    }

    private fun isNeedHightLight(psiElement:PsiElement):Boolean{
        val content = psiElement.text
        val substring = content.substring(1, content.length - 1)
        return substring.startsWith("zeke")
    }
}