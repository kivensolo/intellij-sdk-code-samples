package org.intellij.samples.psi.reference

import com.intellij.psi.PsiElement

interface IURIPrefixHandler<T : PsiElement> {
    /**
     * 前缀列表
     */
    val prefixes:List<String>

    /**
     * 根据元素获取URI的文本内容
     */
    fun getURIText(element: T): String?
}