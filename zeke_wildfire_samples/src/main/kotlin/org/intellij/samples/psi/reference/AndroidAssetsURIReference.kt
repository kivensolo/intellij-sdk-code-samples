package org.intellij.samples.psi.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import org.intellij.utils.findFilesInAssets
import org.jetbrains.android.facet.AndroidFacet

/**
 * 集成PsiReferenceBase，用于自定义实现的一个Android资源的 URI 引用解析器。
 * 同时继承 PsiPolyVariantReference 接口，实现多解析。
 *
 * ElementManipulators.getValueTextRange(element)
 */
class AndroidAssetsURIReference(
    element: PsiElement,
    textRange: TextRange
): PsiReferenceBase<PsiElement>(element,textRange), PsiPolyVariantReference {
    private var key: String? = null
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
        // 我这里针对android项目做处理
        val androidFacet = AndroidFacet.getInstance(element) ?: return ResolveResult.EMPTY_ARRAY
        val rangeInElement = rangeInElement
        val text = myElement.text
        val map = androidFacet.findFilesInAssets(text).map {
            PsiElementResolveResult(it)
        }
        return map.toTypedArray()
    }
}