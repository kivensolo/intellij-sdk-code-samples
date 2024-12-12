package org.intellij.samples.psi.reference.provider

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import org.intellij.samples.psi.reference.AndroidAssetsURIReference
import org.intellij.samples.psi.reference.IURIPrefixHandler

abstract class AndroidAssetsURIReferenceProvider<T : PsiElement> : TypedReferenceProvider<T>(),
    IURIPrefixHandler<T> {

    abstract override fun getURIText(element: T): String?

    override fun getReferences(element: T, context: ProcessingContext): Array<PsiReference> {
        val uriString = getURIText(element)?:return PsiReference.EMPTY_ARRAY
        val prefix:String? = prefixes.find { prefix -> uriString.startsWith(prefix) }

        return prefix?.let {fullPath ->
            // eg: “<prefix>/plugin/settingV3/images/wifi/tip_wifi_close.png”
            var relativePath = uriString.substring(fullPath.length)
            if (relativePath.startsWith('/')) {
                relativePath = relativePath.substring(1)
            }
            // eg: “plugin/settingV3/images/wifi/tip_wifi_close.png”
            val prefixStart = element.text.indexOf(fullPath)
            val prefixEnd = prefixStart + fullPath.length

            val prefixTextRange = TextRange(prefixStart, prefixEnd)
            val resultReferenceList = mutableListOf<PsiReference>(
                //Prefix psiDirectory reference
                AndroidAssetsURIReference(psiElement = element, textRange = prefixTextRange)
            )
            if (relativePath.isEmpty()) {
                return@let resultReferenceList.toTypedArray()
            }

            var startOffset = prefixTextRange.endOffset + 1
            var endOffset: Int
            // 以分隔符将路径进行分割保存
            relativePath.split("/").mapTo(resultReferenceList) { segment: String ->
                // 按照多级路径，创建多个 androidAssetsURIReference
                endOffset = startOffset + segment.length
                val androidAssetsURIReference = AndroidAssetsURIReference(
                    psiElement = element,
                    textRange = TextRange(startOffset, endOffset),
                    prifixEnd = prefixEnd
                )
                startOffset = endOffset + 1 //move to next segment
                androidAssetsURIReference
            }.toTypedArray()
        } ?: PsiReference.EMPTY_ARRAY
    }
}