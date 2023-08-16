// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.samples.live_templates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

/**
 * 模板的自定义markDwon上下文类型定义
 *
 * https://plugins.jetbrains.com/docs/intellij/template-support.html
 *
 * 定义 MarkdownContext 之后，请确保将新的上下文类型Markdown添加到前一步骤创建的 LiveTemplate 配置文件Markdown.xml中。
 *
 * 并不总是需要定义您自己的 TemplateContextType，因为在 IntelliJ 平台中已经定义了许多现有的模板上下文。
 *
 * 如果要将语言支持扩展到现有区域，请考虑重用从 TemplateContextType 继承的众多现有模板上下文类型之一。
 */
open class MarkdownContext : TemplateContextType("Markdown","Markdown") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        return templateActionContext.file.name.endsWith(".md")
    }
}
