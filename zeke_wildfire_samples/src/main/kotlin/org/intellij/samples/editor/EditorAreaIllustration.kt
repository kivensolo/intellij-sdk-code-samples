// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.samples.editor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages

/**
 * If conditions support it, makes a menu visible to display information about the caret.
 * 条件符合的情况下，使菜单显示插入符号的信息
 *
 * @see AnAction
 */
class EditorAreaIllustration : AnAction() {
    /**
     * Displays a message with information about the current caret.
     *
     * @param e Event related to this action
     */
    override fun actionPerformed(e: AnActionEvent) {
        // Get access to the editor and caret model. update() validated editor's existence.
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel = editor.caretModel
        // Getting the primary caret ensures we get the correct one of a possible many.
        val primaryCaret = caretModel.primaryCaret
        // Get the caret information
        val logicalPos = primaryCaret.logicalPosition
        val visualPos = primaryCaret.visualPosition
        val caretOffset = primaryCaret.offset
        // Build and display the caret report.
        val report = """
             $logicalPos
             $visualPos
             Offset: $caretOffset
             """.trimIndent()
        Messages.showInfoMessage(report, "Caret Parameters Inside The Editor")
    }

    /**
     * Sets visibility and enables this action menu item if:
     *
     *  * a project is open
     *  * an editor is active
     *
     *
     * @param e Event related to this action
     */
    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        // Set visibility only in case of existing project and editor
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }
}
