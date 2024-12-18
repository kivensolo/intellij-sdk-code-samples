package org.intellij.samples.hightlight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter
import com.intellij.psi.PsiFile

/**
 * 在对应语言的高亮信息匹配后，进一步判断是否接收此高亮信息，开发需重点对filterHighlightInfo的情况进行关注。
 * 默认不做处理的情况下，accept函数返回值需要返回true。
 */
abstract class BaseHighlightInfoFilter: HighlightInfoFilter {

    /**
     * 框架回调函数
     * @param info 高亮信息
     * @param file 当前psi文件(可能为空)
     * @return  如果需要过滤(不接收)高亮信息，返回false即可
     */
    override fun accept(highlightInfo: HighlightInfo, file: PsiFile?): Boolean {
        if(file == null){
            return false
        }
        file.findElementAt(highlightInfo.getStartOffset()) ?: return false
        return !filterHighlightInfo(highlightInfo,file)
    }

    abstract fun filterHighlightInfo(highlightInfo: HighlightInfo, file: PsiFile): Boolean
}