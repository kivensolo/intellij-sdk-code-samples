package org.intellij.samples.psi.reference.contributor

import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.intellij.samples.psi.reference.provider.JvmLiteralReferenceProvider

class MyJavaReferenceContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val psiPatterns = XmlPatterns.psiElement().inside(
            XmlPatterns.xmlTag().withName("starcor.xul", "language")
        )
        registrar.registerReferenceProvider(psiPatterns , JvmLiteralReferenceProvider<PsiElement>())
    }
}