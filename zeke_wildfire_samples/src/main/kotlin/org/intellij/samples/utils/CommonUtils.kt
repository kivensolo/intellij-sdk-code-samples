package org.intellij.samples.utils

import com.intellij.codeInsight.AutoPopupController
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

object CommonUtils {
    /**
     * 主动触发弹出自动补全的提示PoP框
     */
    fun autoShowCompletionPopup(project: Project, editor: Editor){
        AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
    }
}