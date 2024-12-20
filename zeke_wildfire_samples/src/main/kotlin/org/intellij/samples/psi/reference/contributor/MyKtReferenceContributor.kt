package org.intellij.samples.psi.reference.contributor

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.intellij.samples.psi.reference.provider.JvmLiteralReferenceProvider
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

class MyKtReferenceContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val ktPattern = PlatformPatterns.psiElement(KtStringTemplateExpression::class.java) //表示Java字面量表达式
            .withLanguage(KotlinLanguage.INSTANCE)
        registrar.registerReferenceProvider(ktPattern , JvmLiteralReferenceProvider<PsiElement>())
    }
}