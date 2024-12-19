package org.intellij.samples.hightlight.filter.strategy

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey

/**
 * 抽象策略角色
 */
abstract class BaseFilterStrategy: IFilterStrategy {
    /**
     * 策略支持的高亮级别
     * eg: [HighlightSeverity.ERROR]
     */
    abstract fun supportSeverity(): Array<HighlightSeverity>

    /**
     * 策略支持的高亮显示类型
     * 参见: [TextAttributesKey.createTextAttributesKey] 和 [CodeInsightColors]
     */
    abstract fun supportTextAttributesKey(): Array<TextAttributesKey>

    /**
     * 判定具体策略的描述与高亮元素的描述信息是否相同
     * (偏业务逻辑)
     *
     * @return
     *  true:当前级别的高亮描述信息与具体策略的描述判断符合
     *  false:当前级别的高亮描述信息与具体策略的描述判断不符合
     */
    abstract fun analyseDescription(description: String?, highlightedText: String): Boolean
}