package org.intellij.samples.psi.reference.provider

import com.intellij.diagnostic.LoadingState
import com.intellij.ide.browsers.*
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.FakePsiElement
import com.intellij.util.ProcessingContext

/**
 * 一个zeke://协议的引用提供，若浏览器设置为首选模式，则使用首选浏览器进行跳转操作。
 * 如果没有设置首选模式，那就让windows自己弹窗选择目标应用。
 */
class ZekeProtocolReferenceProvider: TypedReferenceProvider<PsiElement>() {

    override fun acceptsTarget(target: PsiElement): Boolean {
        return false //自定义协议不指向任何真正的PsiElement
    }

    /**
     * 根据PsiElement进行只有逻辑处理，满足条件后，返回自定义的PsiReference
     */
    override fun getReferences(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if(element !is JsonStringLiteral) return PsiReference.EMPTY_ARRAY
        val parent:PsiElement = element.parent
        if (parent !is JsonProperty) return PsiReference.EMPTY_ARRAY

        val jsonValueElement: JsonValue? = parent.value
        if(element != jsonValueElement) return PsiReference.EMPTY_ARRAY
        // JSON may be used as data format for huge strings
        if (element.textLength > 1000) return PsiReference.EMPTY_ARRAY
        if (!element.textContains(':')) return PsiReference.EMPTY_ARRAY;

        //json字符串内容
        val textValue = element.value
        if(textValue.startsWith("zeke://")){
//            val valueTextRange = TextRange.create("zeke://".length +  1,textValue.length + 1)
//            val valueTextRange = TextRange.from(1, textValue.length + 1)
            //TODO 这是在计算什么范围？
            val valueTextRange:TextRange = ElementManipulators.getValueTextRange(element)
            if (valueTextRange.isEmpty) return PsiReference.EMPTY_ARRAY
            return arrayOf(FakePsiElementReference(element,valueTextRange))
        }
        return PsiReference.EMPTY_ARRAY
    }

    /**
     * 一个虚假的PSIElement
     */
    class FakePsiElementReference(
        private val element: PsiElement,
        private var textRange: TextRange?
    ): PsiReferenceBase<PsiElement>(element,textRange) {
        private var stringContent: String = ""

        init {
            textRange?.let {
                stringContent = element.text.substring(it.startOffset, it.endOffset)
            }
        }

        override fun resolve(): PsiElement {
            return OpenBrowseFakePsiElement()
        }

        override fun getCanonicalText(): String {
            return element.text
        }

        override fun getRangeInElement(): TextRange {
            if(textRange == null){
                textRange = calculateDefaultRangeInElement();
            }
            return textRange!!
        }

        fun getUrl(): String {
            return stringContent
        }

        /**
         * 一个假的PSIElement，导航跳转到浏览器
         */
        inner class OpenBrowseFakePsiElement : FakePsiElement(), SyntheticElement {
            override fun getParent(): PsiElement {
                return myElement
            }

            override fun navigate(requestFocus: Boolean) {
                var effectiveBrowser: WebBrowser? = null
                val browserManager = WebBrowserManager.getInstance()
                // 默认的浏览器配置策略，通过seeting/tools/web browser and preview 来设置
                if (browserManager.defaultBrowserPolicy == DefaultBrowserPolicy.FIRST) {
                    effectiveBrowser = browserManager.firstActiveBrowser
                }
                getBrowserLauncher().browse(getUrl(), browser = effectiveBrowser)
            }

            private fun getBrowserLauncher(): BrowserLauncher {
                return if (LoadingState.COMPONENTS_LOADED.isOccurred) BrowserLauncher.instance else BrowserLauncherAppless()
            }

            override fun getPresentableText(): String {
                return getUrl()
            }

            override fun getName(): String {
                return getUrl()
            }

            override fun getTextRange(): TextRange {
                val rangeInElement: TextRange = rangeInElement
                val elementRange: TextRange = myElement.textRange
                return rangeInElement.shiftRight(elementRange.startOffset)
            }
        }


    }
}


