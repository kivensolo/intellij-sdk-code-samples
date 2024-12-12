package org.intellij.samples.psi.reference.provider

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import java.lang.ClassCastException

/**
 * 简单封装的引用提供者，对实现类进行了异常捕获的封装和类型校验的封装
 */
abstract class TypedReferenceProvider<T : PsiElement> : PsiReferenceProvider() {
    final override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        return try {
            getReferences(element as T, context)
        } catch (e: ClassCastException) {
            PsiReference.EMPTY_ARRAY
        }
    }
    abstract fun getReferences(element: T, context: ProcessingContext): Array<PsiReference>
}