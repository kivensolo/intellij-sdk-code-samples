package org.intellij.samples.psi.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken

/**
 * 可应用于xulApp框架的资源规范的引用提供者，
 * 主要对特定的scheme前缀资源路径做引用解析。
 */
class XulAssetsURIRefrenceProvider: AndroidAssetsURIReferenceProvider<PsiElement>() {
    override val prefixes: List<String>
        get() = listOf("file:///.assets")

    override fun getURIText(element: PsiElement): String? {
        return when (element) {
            is XmlToken -> element.text
            is XmlAttributeValue -> element.value
            else -> null
        }
    }
}