package org.intellij.samples.psi.reference

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.json.json5.Json5FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * 集成PsiReferenceBase，用于自定义实现的一个简单的引用解析器。
 * 同时继承 PsiPolyVariantReference 接口，实现多解析。
 *
 * ElementManipulators.getValueTextRange(element)
 *
 * TODO 参考  JsonWebReferenceContributor 和 WebReference
 */
class SimpleJsonReference(
    element: PsiElement,
    textRange: TextRange
): PsiReferenceBase<PsiElement>(element,textRange), PsiPolyVariantReference {
    private val urlContent: String

    init{
        urlContent = element.text.substring(textRange.startOffset, textRange.endOffset)
    }
    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.isEmpty()) {
            null
        } else {
            resolveResults[0].element
        }
    }

    /**
     * 获取可显示的标识符，用于代码补全。
     *
     * @return 包含可见标识符(visible identifiers)的字符串数组
     * 返回的数组用于构建基本代码补全的查找列表。
     */
    override fun getVariants(): Array<Any> {
        val variants: List<LookupElement> = ArrayList()

        //TODo myElement是哪一个元素？
        val project = myElement.project
        val virtualFiles:Collection<VirtualFile> = FileTypeIndex.getFiles(Json5FileType.INSTANCE, GlobalSearchScope.allScope(project))
        virtualFiles.forEach { virtualFile ->
            val findFile = PsiManager.getInstance(project).findFile(virtualFile)

        }
        return variants.toTypedArray()
    }

    override fun getCanonicalText(): String {
        return element.text
    }

    fun isHttpRequestTarget(): Boolean {
        return true
    }

    fun getUrl(): String {
        return urlContent
    }

    /**
     * 进行多解析判断，返回多个结果
     *
     * @return PsiElementResolveResult的数组对象
     * PsiElementResolveResult中，包含目标psiElement
     */
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        if (incompleteCode) {
            return ResolveResult.EMPTY_ARRAY
        }
        val project:Project = myElement.project
        //在工程中招Json类型文件的虚拟文件
        val virtualFiles:Collection<VirtualFile> = FileTypeIndex.getFiles(Json5FileType.INSTANCE, GlobalSearchScope.allScope(project))
        val results: List<ResolveResult> = java.util.ArrayList()
        virtualFiles.forEach { virtualFile ->
            //TODO 类型是什么？？
            val findFile = PsiManager.getInstance(project).findFile(virtualFile)
            //TODo 如果条件合适，就往results中add(new PsiElementResolveResult(fileObj)
        }
        return results.toTypedArray<ResolveResult>()
    }
}