package org.intellij.samples.psi.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlToken
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

/**
 * 提供任意psiElement范围中满足XulApp规范的AndroidAssets资源URI的引用
 * 可以实现在java/kotlin代码中，点击"file:///.assets/images/twtv/preview_tip_icon.png"这种字符串，可以跳转到目标文件。
 *
 */
class XulAppAssetsURIReferenceProvider : AndroidAssetsURIReferenceProvider<PsiElement>() {
    override val prefixes: List<String>
        get() = listOf("file:///.assets")

    override fun getURIText(element: PsiElement): String? {
        when (element) {
            is PsiLiteralExpression -> return element.value as? String
            is KtStringTemplateExpression -> {
                val entries = element.entries
                if (entries.size == 1) {
                    val entry = entries[0]
                    if (entry is KtLiteralStringTemplateEntry) {
                        return entry.text
                    }
                }
                return null
            }
            is XmlToken -> return element.text
            is XmlAttributeValue -> return element.value
            else -> return null
        }
    }

}