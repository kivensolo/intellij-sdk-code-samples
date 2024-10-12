package org.intellij.samples.line_marker

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import java.awt.Color

class MDFileColorProvider : ElementColorProvider{
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