package org.intellij.samples.psi.reference.contributor

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.intellij.samples.psi.reference.provider.XulAppAssetsURIReferenceProvider
import org.intellij.samples.psi.reference.provider.ZekeProtocolReferenceProvider


/**
 * 最基本的引用提供者
 *
 * [com.intellij.psi.PsiReferenceContributor] 用于注册自定义的 [com.intellij.psi.PsiReferenceProvider]
 * ，以便在特定的上下文中识别和处理自定义的引用。
 */
class MySimpleReferenceContributor: PsiReferenceContributor()  {

    /**
     * 注册各个引用提供者
     *
     * 通过registrar.registerReferenceProvider(ElementPattern,Provider) 进行提供者注册。
     *
     * 其中需要提供一个 ElementPattern 来进一步细化匹配规则，
     * 可参见{@link com.intellij.patterns.StandardPatterns}、
     * {@link com.intellij.patterns.PlatformPatterns}及其扩展点的玩法, 如 XmlPatterns 扩展点。
     *
     * <a href="https://plugins.jetbrains.com/docs/intellij/element-patterns.html">IntelliJ Platform Docs</a>
     */
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
//        val psiPatterns = XmlPatterns.psiElement().inside(
//            XmlPatterns.xmlTag().withName("starcor.xul", "language")
//        )
////        val xmlTagPatterns = XmlPatterns.xmlTag().withName("starcor.xul")
//        registrar.registerReferenceProvider(psiPatterns , XulAssetsURIRefrenceProvider())
        registrar.registerReferenceProvider(PlatformPatterns.psiElement() , XulAppAssetsURIReferenceProvider())

        //只需要匹配Json的字符串文本
        val mJsonStringLiteralPattern = PlatformPatterns.psiElement(JsonStringLiteral::class.java)
        registrar.registerReferenceProvider(
            mJsonStringLiteralPattern,
            ZekeProtocolReferenceProvider(),
            PsiReferenceRegistrar.LOWER_PRIORITY
        )
//        registrar.registerReferenceProvider(PlatformPatterns.psiElement() , JvmExpressionReferenceProvider<PsiElement>())
    }
}
