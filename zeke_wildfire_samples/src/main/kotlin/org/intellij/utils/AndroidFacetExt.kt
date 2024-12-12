package org.intellij.utils

import com.android.tools.idea.projectsystem.sourceProviders
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.ResolveResult
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * 根据资源路径查找PSI文件
 */
fun AndroidFacet.findFilesInAssets(relativePath: String): List<PsiFileSystemItem> {
    return sourceProviders.sources.assetsDirectories.mapNotNull {
        val virtualFile = it.findFileByRelativePath(relativePath)?:return@mapNotNull null
        return@mapNotNull if(virtualFile.isDirectory){
            virtualFile.toPsiDirectory(module.project)
        }else{
            virtualFile.toPsiFile(module.project)
        }
    }
}

fun AndroidFacet.getAssetsDirPSIResolve(): List<ResolveResult> {
    val assetsDirectories = sourceProviders.sources.assetsDirectories
    return assetsDirectories.mapNotNull { virtualFile ->
        //每一个资源目录都对应一个virtualFile，如果是目录，就存一个PsiElementResolveResult
        val psiDirectory: PsiDirectory? = virtualFile.toPsiDirectory(module.project)
        psiDirectory?.let { PsiElementResolveResult(it)}
    }
}
