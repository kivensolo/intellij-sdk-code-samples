// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.samples.editor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

/**
 * 右键打开菜单后，通过该演示Action，用固定字符串"Replacement"替换所选字符
 *
 * @see AnAction
 */
class EditorIllustrationAction : AnAction() {

    /**
     * 用固定字符串替换主插入符号所选的文本行。
     *
     * @param e 与此操作相关的事件
     */
    override fun actionPerformed(e: AnActionEvent) {
        // 从data keys中获取所有需要的数据
        // Editor and Project were verified in update(), so they are not null.
        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = e.getRequiredData(CommonDataKeys.PROJECT)
        val document = editor.document
        // Work off of the primary caret to get the selection info
        val primaryCaret = editor.caretModel.primaryCaret
        val start = primaryCaret.selectionStart
        val end = primaryCaret.selectionEnd
        // Replace the selection with a fixed string.
        // Must do this document change in a write action context.
        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(start, end, "Replacement")
        }
        // De-select the text range that was just replaced
        primaryCaret.removeSelection()
    }

    /**
     * Sets visibility and enables this action menu item if:
     *
     *  * a project is open
     *  * an editor is active
     *  * some characters are selected
     *
     *
     * @param e Event related to this action
     */
    override fun update(e: AnActionEvent) {
        // Get required data keys
        val project: Project? = e.project
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        // Set visibility and enable only in case of existing project and editor and if a selection exists
        val enable = project != null && editor != null && editor.selectionModel.hasSelection()
        e.presentation.isEnabledAndVisible = enable
    }
}
