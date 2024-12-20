package org.intellij.samples.psi.reference

import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.asJava.classes.KtLightClassImpl
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry


/**
 * 为传入的psi元素，查找符合条件的java语言的字符串文本引用
 */
class JvmLiteralReference(
    private val psiElement: PsiElement,
    private val targetPackages: List<String>
) : PsiPolyVariantReferenceBase<PsiElement>(psiElement) {
    val aaaaaaa = "asda"
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        if(incompleteCode){
            return ResolveResult.EMPTY_ARRAY
        }
        val path = psiElement.text
        val project = element.project
        val results = mutableListOf<ResolveResult>()

        val psiFacade = JavaPsiFacade.getInstance(project)
        val scope = GlobalSearchScope.allScope(project)
        targetPackages.forEach {
            val clazz = psiFacade.findClass(it, scope)?:return@forEach
            if(isKotlinClass(clazz)){
                // 查找Kotlin文件中的字符串常量
                val kotlinResults = findInKotlinFiles(clazz, path)
                results.addAll(kotlinResults)
            }else{
                // 查找Java文件中的字符串常量
                val javaResults = findInJavaFiles(clazz, path)
                results.addAll(javaResults)
            }
        }
        return results.toTypedArray()
    }

    private fun findInJavaFiles(clazz: PsiClass, target: String): List<ResolveResult> {
        val results = mutableListOf<ResolveResult>()
        //查找目标类下所有的PsiLiteralExpression类型元素(也就是Java字符串表达式)
        val literalElements = PsiTreeUtil.collectElementsOfType(clazz, PsiLiteralExpression::class.java)
        val filterStringElements = literalElements.map {
            it.firstChild  // PsiLiteralExpression 节点下包了一层
        }.filter {
            it.elementType == JavaTokenType.STRING_LITERAL //筛选元素类型为Java字符串的
        }.filter {
            // 筛选文本是目标内容的元素
            val textWithNoQuote = it.text.replace("\"", "")
            textWithNoQuote == target
        }

        filterStringElements.forEach {
            //检查是否是定义为 PsiFiled 的字符串
            val filedOfStringLiteral = it.parent?.parent
            if(filedOfStringLiteral != null && filedOfStringLiteral is PsiField){
                results.add(PsiElementResolveResult(filedOfStringLiteral))
            }else{
                results.add(PsiElementResolveResult(it))
            }
        }
        return results
    }

    private fun isKotlinClass(psiClass: PsiClass): Boolean {
        // 检查是否为 Kotlin 生成的轻量级类
        return psiClass is KtLightClassImpl
    }

    private fun findInKotlinFiles(clazz: PsiClass, target: String): List<ResolveResult> {
        val results = mutableListOf<ResolveResult>()
        val aaaaaaa = "asda"

        val fields = clazz.fields
        fields.forEach {
            print(it.elementType)
        }
        //FIXME 这个类型找不对
        val literalElements = PsiTreeUtil.collectElementsOfType(clazz, KtLiteralStringTemplateEntry::class.java)
        val filterStringElements = literalElements.map {
            it.firstChild  // KtLiteralStringTemplateEntry 节点下包了一层
        }.filter {
            it.elementType == KtTokens.REGULAR_STRING_PART //筛选元素类型为kotlin字符串的
        }.filter {
            // 筛选文本是目标内容的元素
            it.text == target
        }

        filterStringElements.forEach {
            //检查是否是定义为 PsiFiled 的字符串
            results.add(PsiElementResolveResult(it))
        }
        return results
    }

}