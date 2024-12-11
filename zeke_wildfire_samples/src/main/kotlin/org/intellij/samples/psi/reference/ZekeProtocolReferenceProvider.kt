package org.intellij.samples.psi.reference

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

/**
 * 创建引用提供者
 */
class ZekeProtocolReferenceProvider: PsiReferenceProvider() {

    override fun acceptsTarget(target: PsiElement): Boolean {
        return false //自定义协议不指向任何真正的PsiElement
    }

    /**
     * 根据PsiElement进行只有逻辑处理，满足条件后，返回自定义的PsiReference
     */
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if(element !is JsonStringLiteral) return PsiReference.EMPTY_ARRAY
        val parent:PsiElement = element.parent
        if (parent !is JsonProperty) return PsiReference.EMPTY_ARRAY

        val jsonValueElement: JsonValue? = parent.value
        if(element != jsonValueElement) return PsiReference.EMPTY_ARRAY
        // JSON may be used as data format for huge strings
        if (element.getTextLength() > 1000) return PsiReference.EMPTY_ARRAY
        if (!element.textContains(':')) return PsiReference.EMPTY_ARRAY;

        //json字符串内容
        val textValue = element.value
        if(textValue.startsWith("zeke://")){
//            val valueTextRange = TextRange.create("zeke://".length +  1,textValue.length + 1)
//            val valueTextRange = TextRange.from(1, textValue.length + 1)
            //TODO 这是在计算什么范围？
            val valueTextRange:TextRange = ElementManipulators.getValueTextRange(element);
            if (valueTextRange.isEmpty) return PsiReference.EMPTY_ARRAY
            return arrayOf(FakePsiElementReference(element,valueTextRange))
        }
        return PsiReference.EMPTY_ARRAY
    }
}