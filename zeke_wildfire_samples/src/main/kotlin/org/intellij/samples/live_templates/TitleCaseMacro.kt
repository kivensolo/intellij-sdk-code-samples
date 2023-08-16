// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.samples.live_templates

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.macro.MacroBase
import com.intellij.openapi.util.text.StringUtil

/**
 * 自定义的live templates中的宏
 * 将每个单词的首字母转成大些
 *
 * [官方文档](https://plugins.jetbrains.com/docs/intellij/new-macros.html)
 *
 * 使用方式：
 * Highlight the text and enter invok: Code | Surround With...
 * to open the Surround With popup.
 * Confirm that the SDK: Convert to title case
 * is available in the popup, and select it.
 *
 * 最开始还以为直接输入“mc”后，再输入文字就ok了，实则并不是。
 */
open class TitleCaseMacro : MacroBase {
    constructor() : super("titleCase", "titleCase(String)")

    private constructor(name: String, description: String) : super(name, description)

    override fun calculateResult(
        params: Array<Expression>,
        context: ExpressionContext,
        quick: Boolean
    ): Result? {
        // Retrieve the text from the macro or selection, if any is available.
        var text = getTextResult(params, context, true) ?: return null
        if (text.isNotEmpty()) {
            // 将每个单词的首字母转成大写
            text = StringUtil.toTitleCase(text)
        }
        return TextResult(text)
    }

    /**
     * 是否是在Markdown的上下文中
     */
    override fun isAcceptableInContext(context: TemplateContextType): Boolean {
        // Might want to be less restrictive in future
        return context is MarkdownContext
    }
}
