package org.intellij.samples.psi.reference.provider

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import org.intellij.samples.psi.reference.AndroidAssetsURIReference
import org.intellij.samples.psi.reference.IURIPrefixHandler

/**
 * 针对Android工程，进行相关资源(如assets)的引用提供
 */
abstract class AndroidAssetsURIReferenceProvider<T : PsiElement> : TypedReferenceProvider<T>(),
    IURIPrefixHandler<T> {

    abstract override fun getURIText(element: T): String?

    override fun getReferences(element: T, context: ProcessingContext): Array<PsiReference> {
        val uriString = getURIText(element)?:return PsiReference.EMPTY_ARRAY
        val prefix:String? = prefixes.find { prefix -> uriString.startsWith(prefix) }

        return prefix?.let {prefixPath ->
            // eg: “<prefix>/plugin/settingV3/images/wifi/tip_wifi_close.png”
            var relativePath = uriString.substring(prefixPath.length)
            if (relativePath.startsWith('/')) {
                relativePath = relativePath.substring(1)
            }
            // eg: “plugin/settingV3/images/wifi/tip_wifi_close.png”

            //Build prefix textRange info
            val prefixStart = element.text.indexOf(prefixPath)
            val prefixEnd = prefixStart + prefixPath.length
            val prefixTextRange = TextRange(prefixStart, prefixEnd)

            val resultReferenceList = mutableListOf<PsiReference>(
                //Prefix psiDirectory reference
                AndroidAssetsURIReference(
                    psiElement = element,
                    elementTextRange = prefixTextRange,
                    prifixRange = prefixTextRange
                )
            )
            if (relativePath.isEmpty()) {
                return@let resultReferenceList.toTypedArray()
            }

            var startOffset = prefixTextRange.endOffset + 1
            var endOffset: Int
            /*
             * 以分隔符把路径进行分别处理，将每一个路径段处理为单独的引用
             * 最终把引用转为PSI数组返回
             */
            relativePath.split("/").mapTo(resultReferenceList) { segment: String ->
                // 按照多级路径，创建多个 androidAssetsURIReference
                endOffset = startOffset + segment.length
                //TODO 可以做一个开关设置
                val androidAssetsURIReference = AndroidAssetsURIReference(
                    psiElement = element,
                    elementTextRange = TextRange(prefixTextRange.startOffset, endOffset), //引用元素是连续选择的内容
//                  referenceTextRange = TextRange(startOffset, endOffset), //引用元素是单个路径片段内容
                    prifixRange = prefixTextRange
                )
                startOffset = (endOffset + 1) //move to next segment
                androidAssetsURIReference
            }.toTypedArray()
        } ?: PsiReference.EMPTY_ARRAY
    }
}