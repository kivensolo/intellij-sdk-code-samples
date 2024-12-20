package org.intellij.samples.psi.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlToken
import com.intellij.util.ProcessingContext
import org.intellij.samples.psi.reference.IURIPrefixHandler
import org.intellij.samples.psi.reference.JvmLiteralReference

class JvmLiteralReferenceProvider<T : PsiElement> : TypedReferenceProvider<T>(),
    IURIPrefixHandler<T> {
    override val prefixes: List<String>
        get() =  listOf("file:///.app")

    // 引用类提供的范围
    private val referenceProviderScope:List<String> = listOf("com.starcor.base.StarcorApp", "com.starcor.base.StarcorApp2")

    override fun getURIText(element: T): String? {
        return when (element) {
            is XmlToken -> element.text
            is XmlAttributeValue -> element.value
            else -> null
        }
    }

    override fun getReferences(element: T, context: ProcessingContext): Array<PsiReference> {
        val contentString = getURIText(element)?:return PsiReference.EMPTY_ARRAY
        val noQuoteString = contentString.replace("\"", "")
        var isStartWithPrefix = false
        prefixes.forEach { prefix ->
            isStartWithPrefix = noQuoteString.startsWith(prefix)
            if(isStartWithPrefix){
                return@forEach
            }
        }
        if(!isStartWithPrefix){
            return PsiReference.EMPTY_ARRAY
        }
        //查找对应的目标范围代码里的字符串
        return arrayOf(JvmLiteralReference(element, referenceProviderScope))
    }
}