package org.intellij.samples.psi.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

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
            return arrayOf(MyJsonSimpleUriPsiReference(element))
        }
        return PsiReference.EMPTY_ARRAY
    }
}