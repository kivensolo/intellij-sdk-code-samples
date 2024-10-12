package org.intellij.samples.line_marker

import com.intellij.codeInsight.daemon.*
import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.ui.ColorChooser
import com.intellij.ui.ColorPicker
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.picker.ColorListener
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.Function
import com.intellij.util.FunctionUtil
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.ColorsIcon
import java.awt.Color
import java.awt.event.MouseEvent
import javax.swing.Icon

/**
 * 为markdown文件创建颜色识别的Gutter图标
 */
class MDColorLineMarkerProviderDescriptor : LineMarkerProviderDescriptor() {
//    val INSTANCE = MarkDownColorLineMarkerProvider()

    override fun getName(): String {
        return "markdown的取色器图标"
    }

    override fun getIcon(): Icon {
        return AllIcons.Gutter.Colors
    }


    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return null
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        for (element in elements) {
            ElementColorProvider.EP_NAME.computeSafeIfAny { provider: ElementColorProvider ->
                val color = provider.getColorFrom(element) ?: return@computeSafeIfAny null
                val info = ColorInfo(element, color, provider)
                NavigateAction.setNavigateAction(
                    info,
                    IdeBundle.message("dialog.title.choose.color"),
                    null,
                    AllIcons.Gutter.Colors
                )
                result.add(info)
                info
            }
        }
    }


    private class ColorInfo(element: PsiElement, private val myColor: Color,colorProvider: ElementColorProvider) :
        MergeableLineMarkerInfo<PsiElement>(element, element.textRange,
            JBUIScale.scaleIcon<ColorIcon>(ColorIcon(12, myColor)),
            FunctionUtil.nullConstant<Any, String>(),
            // 图标导航的Handler
            object : GutterIconNavigationHandler<PsiElement> {
                override fun navigate(e: MouseEvent, elt: PsiElement) {
                    if (!elt.isWritable) return
                    //这里是可能为null哦
                    val editor: Editor = PsiEditorUtil.findEditor(elt)!!
                    if (Registry.`is`("ide.new.color.picker")) {
                        //新版colorPicker
                        val relativePoint = RelativePoint(e.component, e.point)
                        ColorPicker.showColorPickerPopup(
                            element.project, myColor, object : ColorListener {
                                override fun colorChanged(color: Color, source: Any) {
                                    WriteAction.run<RuntimeException> { colorProvider.setColorTo(elt, color) }
                                }
                            },
                            relativePoint, true
                        )
                    }else{
                        //旧版colorPicker
                        val mColor = ColorChooser.chooseColor(
                            editor.project,
                            editor.component,
                            IdeBundle.message("dialog.title.choose.color", *arrayOfNulls(0)),
                            myColor,
                            true
                        )
                        if (mColor != null) {
                            WriteAction.run<java.lang.RuntimeException> {
                                colorProvider.setColorTo( elt, mColor)
                            }
                        }
                    }
                }
            },
            GutterIconRenderer.Alignment.LEFT
        ) {
        override fun canMergeWith(info: MergeableLineMarkerInfo<*>): Boolean {
            return info is ColorInfo
        }

        override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>?>): Icon {
            return JBUIScale.scaleIcon<ColorsIcon>(
                ColorsIcon(12, *infos.mapNotNull { (it as ColorInfo).myColor }.toTypedArray())
            )
        }

        override fun getCommonTooltip(infos: List<MergeableLineMarkerInfo<*>?>): Function<in PsiElement, String> {
            return FunctionUtil.nullConstant()
        }
    }

}