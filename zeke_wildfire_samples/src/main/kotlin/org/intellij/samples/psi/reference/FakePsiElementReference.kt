package org.intellij.samples.psi.reference

import com.intellij.diagnostic.LoadingState
import com.intellij.ide.BrowserUtil
import com.intellij.ide.browsers.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.FakePsiElement

/**
 * 一个虚假的PSIElement
 */
class FakePsiElementReference(
    private val element: PsiElement,
    private var textRange: TextRange?
): PsiReferenceBase<PsiElement>(element,textRange) {
    private var urlContent: String = ""

    init {
        textRange?.let {
            urlContent = element.text.substring(it.startOffset, it.endOffset)
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

    fun isHttpRequestTarget(): Boolean {
        return true
    }

    fun getUrl(): String {
        return urlContent
    }

    /**
     * 一个假的PSIElement，导航跳转到浏览器
     */
    inner class OpenBrowseFakePsiElement : FakePsiElement(), SyntheticElement {
        private var effectiveBrowser: WebBrowser? = null
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

        private fun getBrowserLauncher():  BrowserLauncher{
            return if (LoadingState.COMPONENTS_LOADED.isOccurred) BrowserLauncher.instance else BrowserLauncherAppless()
        }

        override fun getPresentableText(): String {
            return getUrl()
        }

        override fun getName(): String {
            return getUrl()
        }

        override fun getTextRange(): TextRange {
            val rangeInElement: TextRange = getRangeInElement()
            val elementRange: TextRange = myElement.textRange
            return rangeInElement.shiftRight(elementRange.startOffset)
        }
    }


}

