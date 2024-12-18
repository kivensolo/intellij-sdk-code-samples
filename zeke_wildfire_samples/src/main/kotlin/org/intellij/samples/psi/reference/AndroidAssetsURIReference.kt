package org.intellij.samples.psi.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import org.intellij.samples.utils.findFilesInAssets
import org.jetbrains.android.facet.AndroidFacet

/**
 * 集成PsiReferenceBase，用于自定义实现的一个Android资源的 URI 引用解析器。
 * 比如可以导航至“file:///”开头的assets资源文件
 * 同时继承 PsiPolyVariantReference 接口，实现多解析。
 */
class AndroidAssetsURIReference(
    psiElement: PsiElement,
    elementTextRange: TextRange,
    val prifixRange: TextRange
): PsiReferenceBase<PsiElement>(psiElement,elementTextRange), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.isEmpty()) {
            null
        } else {
            resolveResults[0].element
        }
    }

    override fun getCanonicalText(): String {
        return element.text
    }

    /**
     * 进行多解析判断，返回多个结果，结果数据为PsiElementResolveResult类型保存在数组中。
     */
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        if (incompleteCode) {
            return ResolveResult.EMPTY_ARRAY
        }
        val androidFacet = AndroidFacet.getInstance(element) ?: return ResolveResult.EMPTY_ARRAY
        val rangeInElement = rangeInElement
        val elementFullText = myElement.text
        val relativePath = elementFullText.substring( prifixRange.endOffset, rangeInElement.endOffset)
        val psiFilesInAssets = androidFacet.findFilesInAssets(relativePath)

        return psiFilesInAssets.map { PsiElementResolveResult(it) }.toTypedArray()
    }
}