package org.intellij.utils

import com.android.tools.idea.projectsystem.sourceProviders
import com.intellij.psi.PsiFileSystemItem
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
