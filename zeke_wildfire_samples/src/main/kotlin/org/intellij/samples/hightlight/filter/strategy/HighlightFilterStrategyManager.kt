package org.intellij.samples.hightlight.filter.strategy

import com.intellij.codeInsight.daemon.JavaErrorBundle
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.changesHandler.range
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import java.util.*

/**
 * 过滤策略
 */
object HighlightFilterStrategyManager {
    private val registeredStrategies: List<BaseFilterStrategy> = listOf(
        DefaultFilterStrategy(),
        SymbolResolveErrorFilterStrategy()
    )

    /**
     * 执行过滤操作
     * Step-1. 高亮级别判断
     * Step-2. 高亮类型属性key判断(实际判断的是名称), 参见 HighlightInfoType 接口
     * Step-3. 判断高亮描述信息是否判断当前策略
     */
    fun filter(info: HighlightInfo, file: PsiFile?): Boolean {
        if (null == file) {
            return false
        }
        val isFilter:Optional<Boolean> = registeredStrategies.stream()
            .filter { it.supportSeverity().contains(info.severity) }
            .filter { it.supportTextAttributesKey().contains(info.type.attributesKey)}
            .filter { it.analyseDescription(info.description, info.range.substring(file.text))}
            .findFirst() //选择第一个符合条件的策略Optional选项, 即Optional<BaseFilterStrategy>
            .map { strategy ->
                /*
                 * map的入参函数对象，整体为 Function<T, R> 函数
                 * 内部通过 R apply(T var1) 进行调用，T为输入对象BaseFilterStrategy
                 * 返回一个Optional<U>类型， U由最后的表达式决定样式
                 */
                strategy.filter(info, file)
            }
        return isFilter.orElse(false)
    }

}

/**
 * 默认过滤策略
 * 针对所有标准级别，且TextAttributesKey为INFORMATION_ATTRIBUTES的做过滤
 */
open class DefaultFilterStrategy : BaseFilterStrategy() {
    /**
     * Accept all standard severity levels
     */
    override fun supportSeverity(): Array<HighlightSeverity> = HighlightSeverity.DEFAULT_SEVERITIES

    override fun supportTextAttributesKey(): Array<TextAttributesKey> {
        return arrayOf(CodeInsightColors.INFORMATION_ATTRIBUTES)
    }

    override fun analyseDescription(description: String?, highlightedText: String): Boolean {
        return true
    }

    override fun filter(info: HighlightInfo, file: PsiFile): Boolean {
        val highlightedElement = file.findElementAt(info.getStartOffset()) ?: return false
        val content = highlightedElement.text
        // 这里只是简单的根据psiElement的文本内容来判断过滤
        return content == "\"DisableHighlight\""
    }

}

/**
 * 自定义过滤策略
 *
 * 这里过滤掉xul框架布局文件中所有的class属性的 `cannot.resolve.symbol` 错误
 */
class SymbolResolveErrorFilterStrategy : DefaultFilterStrategy() {
    override fun supportSeverity(): Array<HighlightSeverity> = arrayOf(HighlightSeverity.ERROR)

    override fun supportTextAttributesKey(): Array<TextAttributesKey> {
        return arrayOf(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES)
    }
    override fun analyseDescription(description: String?, highlightedText: String): Boolean {
        return JavaErrorBundle.message("cannot.resolve.symbol", highlightedText) == description
    }

    override fun filter(info: HighlightInfo, file: PsiFile): Boolean {
        val highlightedElement = file.findElementAt(info.getStartOffset()) ?: return false
        val parentOfType = PsiTreeUtil.getParentOfType(highlightedElement, XmlAttribute::class.java, true)
        return if (parentOfType == null) {
            false
        } else {
            // 满足拦截的条件，这里简单做一个名称判断
            "class" == parentOfType.name
        }
    }
}