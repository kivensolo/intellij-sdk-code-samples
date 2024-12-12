package org.intellij.samples.psi.reference.provider

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.intellij.samples.psi.reference.SimpleJsonReference

/**
 * 创建引用提供者
 */
class SimpleJsonReferenceProvider: TypedReferenceProvider<JsonStringLiteral>() {

    /**
     * 根据PsiElement进行只有逻辑处理，满足条件后，返回自定义的PsiReference
     */
    override fun getReferences(element: JsonStringLiteral, context: ProcessingContext): Array<PsiReference> {
        //json字符串内容
        val jsonStringContent = element.value
        if(jsonStringContent.startsWith("zeke://")){
            val range = TextRange.create("zeke://".length +  1,jsonStringContent.length + 1)
            return arrayOf(SimpleJsonReference(element,range))
        }
        return PsiReference.EMPTY_ARRAY
    }
}