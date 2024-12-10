package org.intellij.samples.psi.reference

import com.intellij.json.psi.JsonObject
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.jetbrains.jsonSchema.impl.JsonRequiredPropsReferenceProvider.findPropertiesObject
import com.jetbrains.jsonSchema.impl.JsonSchemaBaseReference


class MyJsonSimpleUriPsiReference(element: PsiElement): JsonSchemaBaseReference<PsiElement>(element,
    ElementManipulators.getValueTextRange(element)){

    override fun getCanonicalText(): String {
        return element.text
    }

    /**
     * 返回作为引用目标的元素
     *
     * TODO 返回一个什么引用的PsiElement????
     */
    override fun resolveInner(): PsiElement? {
        val propertiesObject: JsonObject? = findPropertiesObject(element)
        if (propertiesObject != null) {
            val name: String = element.text
            for (property in propertiesObject.propertyList) {
                if (name == property.name) {
                    return property
                }
            }
        }
        return null
    }
}