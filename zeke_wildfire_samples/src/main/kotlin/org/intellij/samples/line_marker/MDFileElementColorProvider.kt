package org.intellij.samples.line_marker

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.*
import java.awt.Color

/**
 * 这个自定义的ElementColorprovider扩展点会被IDE注册。
 * 在XXXXLineMarkerProvider收集LineMarker信息的时候，会调用getColorFrom();
 *
 * 但是现在这个ElementColorProvider还存在bug:
 * 1. 颜色选择后，文本会被全部复写;
 */
class MDFileElementColorProvider : ElementColorProvider{
    private val colorParser = HexColorParser(8)
    private lateinit var lastElement:PsiElement
    private var editorColorElementCahce:HashMap<Editor,PsiElement> = HashMap()
    override fun getColorFrom(element: PsiElement): Color? {
        val isInMarkDownFile = element.context?.containingFile?.name?.endsWith(".md") ?: false
        if (!isInMarkDownFile) return null
        if (element.firstChild != null) return null
        if (element is PsiWhiteSpace) return null
        if (element.text.isEmpty()) return null
//        val project = element.context?.project ?: return null
//        val editor = FileEditorManager.getInstance(project).selectedTextEditor?:return null
        lastElement = element
//        editorColorElementCahce[editor] = lastElement

        colorParser.apply {
            //这个element.text是整个editor的，而不是单独一行的
            val parseredColor = this.parserColor(element.text)
            return parseredColor
        }
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        val project = lastElement.context?.project ?: return
        //        (element as PsiPlainTextImpl).replaceWithText(colorParser.convertColor(color))
        val textColor = colorParser.convertColor(color)
        val newElement  = PsiElementFactory.getInstance(project).createExpressionFromText(textColor, null)
        lastElement.replace(newElement)

//        val editor = FileEditorManager.getInstance(project).selectedTextEditor?:return
//        PsiDocumentManager.getInstance(project).commitDocument(editor.document)
    }

}