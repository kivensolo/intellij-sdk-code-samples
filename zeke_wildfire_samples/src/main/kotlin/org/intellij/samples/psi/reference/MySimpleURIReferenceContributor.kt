package org.intellij.samples.psi.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

/**
 * [com.intellij.psi.PsiReferenceContributor] 用于注册自定义的 [com.intellij.psi.PsiReferenceProvider]
 * ，以便在特定的上下文中识别和处理自定义的引用。
 */
class MySimpleURIReferenceContributor: PsiReferenceContributor()  {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        /**
         * 注册引用提供者
         *
         * 需要提供一个 ElementPattern 来进一步细化匹配规则。
         * 引用点描述：这里使用标准的psiElement参考规则。
         * 参见{@link com.intellij.patterns.StandardPatterns}、
         * {@link com.intellij.patterns.PlatformPatterns}及其扩展点。
         */
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(), CustomSchemeJsonReferenceProvider())
    }
}

/**
 * 创建引用提供者
 */
class CustomSchemeJsonReferenceProvider: PsiReferenceProvider() {
    /**
     * 根据PsiElement进行只有逻辑处理，满足条件后，返回自定义的PsiReference
     */
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if(element !is JsonStringLiteral){
            return PsiReference.EMPTY_ARRAY
        }
        val text = element.text
        val value = element.value
        if(value.startsWith("zeke://")){
            return arrayOf(MySimpleUriPsiReferencee(element))
        }
        return PsiReference.EMPTY_ARRAY
    }
}

class MySimpleUriPsiReferencee(element: PsiElement): PsiReferenceBase<PsiElement>(element){
    /**
     * 返回作为引用目标的元素
     */
    override fun resolve(): PsiElement? {
        //返回一个什么引用的PsiElement?
        return null
//return SimpleJavaMethodFileIndex.getMethodsPsiByName(element.project, text)
    }

    override fun getCanonicalText(): String {
        return element.text
    }
}