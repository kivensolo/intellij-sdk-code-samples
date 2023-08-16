// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.intellij.samples.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * TypedHandlerDelegate扩展点的自定义实现，用于处理编辑器中激活的按键操作。
 * execute方法在文档的偏移量0处插入一个固定的字符串。
 * 文档更改是在写操作的上下文中进行的。
 *
 * This is a custom {@link TypedHandlerDelegate} that handles actions activated keystrokes in the editor.
 * The execute method inserts a fixed string at Offset 0 of the document.
 * Document changes are made in the context of a write action.
 */
class MyTypedHandler extends TypedHandlerDelegate {

  /**
   * 在编辑器中插入用户输入的指定字符后调用。
   */
  @NotNull
  @Override
  public Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    // Get the document and project
    final Document document = editor.getDocument();
    // Construct the runnable to substitute the string at offset 0 in the document
    Runnable runnable = () -> document.insertString(0, "editor_basics:CustomTypedHandler\n");
    // Make the document change in the context of a write action.
    WriteCommandAction.runWriteCommandAction(project, runnable);
    return Result.STOP;
  }

}
