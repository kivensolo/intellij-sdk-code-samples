package org.intellij.samples.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiPackage
import com.intellij.psi.search.GlobalSearchScope

object PackageUtils {
    /**
     * 获取指定包路径及其所有子包下的所有 PsiClass。
     *
     * @param project 当前项目
     * @param packageName 包路径，例如 "com.starcor"
     * @return 包含所有 PsiClass 的列表
     */
    fun getPsiClassesInPackageRecursively(project: Project?, packageName: String?): List<PsiClass> {
        val psiClasses: MutableList<PsiClass> = ArrayList()

        // 获取 JavaPsiFacade 实例
        val psiFacade = JavaPsiFacade.getInstance(project!!)

        // 获取指定包路径的 PsiPackage 对象
        val psiPackage = psiFacade.findPackage(packageName!!)
        if (psiPackage != null) {
            // 递归获取包及其子包中的所有 PsiClass
            collectPsiClasses(
                psiPackage, psiClasses, GlobalSearchScope.allScope(project)
            )
        }
        return psiClasses
    }

    /**
     * 递归收集指定 PsiPackage 及其子包中的所有 PsiClass。
     *
     * @param psiPackage 当前 PsiPackage
     * @param psiClasses 收集的 PsiClass 列表
     * @param scope 搜索作用域
     */
    private fun collectPsiClasses(psiPackage: PsiPackage, psiClasses: MutableList<PsiClass>, scope: GlobalSearchScope) {
        // 获取当前包中的所有 PsiClass
        val classes = psiPackage.getClasses(scope)
        for (psiClass in classes) {
            psiClasses.add(psiClass)
        }

        // 获取当前包的所有子包
        val subPackages = psiPackage.getSubPackages(scope)
        for (subPackage in subPackages) {
            collectPsiClasses(subPackage, psiClasses, scope)
        }
    }
}
