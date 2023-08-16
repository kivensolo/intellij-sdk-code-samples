// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.samples.editor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.actionSystem.EditorActionManager

/**
 * 通过ActionHandler
 * 实现根据现有插入光标，再下一行克隆一个新的光标的菜单操作。
 *
 * @see AnAction
 */
class EditorHandlerIllustration : AnAction() {
    /**
     * Clones a new caret at a higher Logical Position line number.
     *
     * @param e Event related to this action
     */
    override fun actionPerformed(e: AnActionEvent) {
        // Editor is known to exist from update, so it's not null
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        // Get the action manager in order to get the necessary action handler...
        val actionManager = EditorActionManager.getInstance()
        // Get the action handler registered to clone carets
        val actionHandler = actionManager.getActionHandler(IdeActions.ACTION_EDITOR_CLONE_CARET_BELOW)
        // Clone one caret below the active caret
        actionHandler.execute(editor, editor.caretModel.primaryCaret, e.dataContext)
    }

    /**
     * Enables and sets visibility of this action menu item if:
     *
     *  * a project is open
     *  * an editor is active
     *  * at least one caret exists
     *
     *
     * @param e Event related to this action
     */
    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        // Make sure at least one caret is available
        var menuAllowed = false
        if (editor != null && project != null) {
            // Ensure the list of carets in the editor is not empty
            menuAllowed = !editor.caretModel.allCarets.isEmpty()
        }
        e.presentation.isEnabledAndVisible = menuAllowed
    }
}
