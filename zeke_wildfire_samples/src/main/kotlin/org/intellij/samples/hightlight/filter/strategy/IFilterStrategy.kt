package org.intellij.samples.hightlight.filter.strategy

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * 高亮过滤策略接口
 */
interface IFilterStrategy {
    /**
     * 过滤判断，判断高亮元素是否需要屏蔽高亮
     * @param info 高亮信息
     * @param file PsiFile
     * @return true:屏蔽高亮(过滤)，false:保留高亮(不过滤)
     */
    fun filter(info: HighlightInfo, file: PsiFile): Boolean
}