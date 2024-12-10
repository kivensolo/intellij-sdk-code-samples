package org.intellij.samples.psi.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.ProcessingContext

/**
 * 最基本的引用提供者
 *
 * [com.intellij.psi.PsiReferenceContributor] 用于注册自定义的 [com.intellij.psi.PsiReferenceProvider]
 * ，以便在特定的上下文中识别和处理自定义的引用。
 */
class MySimpleURIReferenceContributor: PsiReferenceContributor()  {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        /**
         * 注册引用提供者
         *
         * 需要提供一个 ElementPattern 来进一步细化匹配规则。
         * 引用点描述：这里使用标准的psiElement参考规则。
         * 参见{@link com.intellij.patterns.StandardPatterns}、
         * {@link com.intellij.patterns.PlatformPatterns}及其扩展点。
         */
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(), CustomSchemeJsonReferenceProvider())
    }
}
