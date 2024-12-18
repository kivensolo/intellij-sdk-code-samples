package org.intellij.samples.completion

import com.android.tools.idea.projectsystem.sourceProviders
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import org.intellij.samples.psi.reference.AndroidAssetsURIReference
import org.intellij.samples.utils.CommonUtils
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry

/**
 * 定义一个补全提供者
 */
class AndroidAssetsPathCompletionContributor: CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            MyCompletionProvider()
        )
    }

    /**
     * 自定义的 CompletionProvider
     * 实现的功能为:
     * 1. 当没有"file:///xxxx" 前缀时，输入f开头的数据，则提供一个查询元素。
     * 2. 支持输入完前缀后，如果是androidAssets目录，就自动的弹出候选的补全提示框。
     */
    class MyCompletionProvider: CompletionProvider<CompletionParameters>(){
        /**
         * 添加完整的提供选项
         * @param parameters CompletionParameters或者CompletionParameters的子类。
         *          补全参数，提供getPosition()方法获取PsiElement
         * @param context   处理上下文，可以在各种扩展实现中用于管理临时状态(储存或读取数据)
         * @param resultSet  补全结果提示集，用于添加补全元素
         */
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val originalPosition = parameters.originalPosition ?: return
            val androidFacet = AndroidFacet.getInstance(originalPosition) ?: return
            val elementText = findOriginalTextWithType(originalPosition)

            elementText?.apply { //检查输入的文字
                val typingText = this
                val androidAssetsDirectories = androidFacet.sourceProviders.sources.assetsDirectories

                PrifixConst.xulFilePrifixs.forEach {prefixToLookUp ->
                    val prefixStartIndex: Int = this.indexOf(prefixToLookUp)
                    val notExist = prefixStartIndex < 0
                    if (notExist && typingText.startsWith("f")) {
                        // 输入的内容首字母为"f"时提示"file:///xxxxxx", 所以需要设置自定义的resultSet
                        androidAssetsDirectories.first().let {
                            // 将typingText与目标LookUp元素(prefixToLookUp)作匹配查找
                            resultSet.withPrefixMatcher(typingText)
                                .addElement(createLookupElement(it, prefixToLookUp))
                        }
                    }else{
                        if(PrifixConst.XUL_Assets != prefixToLookUp){
                            return@forEach
                        }

                        //检测输入内容是否是android资源的psi引用对象，如果是，就提示能找到的目标psi元素
                        val psiReference = parameters.position.containingFile.findReferenceAt(parameters.offset)
                        var isAndroidAssetsPsiReference = psiReference is AndroidAssetsURIReference
                        if(psiReference is PsiMultiReference){
                            val reference = psiReference.references.find { it is AndroidAssetsURIReference }
                            isAndroidAssetsPsiReference = (reference != null)
                        }
                        if(isAndroidAssetsPsiReference){
                            val relativePath = typingText.substring(prefixStartIndex + prefixToLookUp.length)
                            val lastSlashIndex = relativePath.lastIndexOf("/")
                            if (lastSlashIndex >= 0) {
                                val newPrefix = relativePath.substring(lastSlashIndex + 1)
                                //找到斜杠之前的资源目录
                                val parentAssetsPath = relativePath.substring(0, lastSlashIndex)
                                for(assetsDir in androidAssetsDirectories){
                                    val virtualFile = assetsDir.findFileByRelativePath(parentAssetsPath) ?: continue
                                    if (virtualFile.isDirectory) {
                                        // 查找文件夹下与输入前缀文字匹配的文件，并添加到提示结果列表
                                        val lookUpEmelemnts = virtualFile.children.map { child ->
                                            createLookupElement(child, child.name)
                                        }
                                        resultSet.withPrefixMatcher(newPrefix).addAllElements(lookUpEmelemnts)
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * 创建一个文本为 "lookupString" 的查询元素
         */
        private fun createLookupElement(file: VirtualFile, lookupString: String): LookupElement {
            val icon = if (file.isDirectory) PlatformIcons.FOLDER_ICON else file.fileType.icon
            return LookupElementBuilder.create(file, lookupString)
                .withIcon(icon)
                //设置lookup元素被选择后的回调函数
                .withInsertHandler { insertionContext, lookupElement ->
                    val editor = insertionContext.editor
                    val pathSeparator = "/"
                    //执行lookUp元素的文本插入逻辑
                    val vFile = lookupElement.getObject() as VirtualFile
                    val document = editor.document
                    val caretEnd = insertionContext.selectionEndOffset

                    // 插入符如果在文档中尾部或者输入的字符串后没有正斜杠，则插入一个正斜杠
                    var needInsertForwardSlash = false
                    if (document.textLength == caretEnd) {
                        needInsertForwardSlash = true
                    } else {
                        if (document.charsSequence[caretEnd].toString() != pathSeparator) {
                            needInsertForwardSlash = true
                        }
                    }
                    if (vFile.isDirectory) {
                        if (needInsertForwardSlash) {
                            document.insertString(caretEnd, pathSeparator)
                        }
                        editor.caretModel.moveToOffset(caretEnd + 1) //光标移动+1
                        CommonUtils.autoShowCompletionPopup(insertionContext.project, editor)
                    }
                }
        }

        private fun findOriginalTextWithType(element: PsiElement): String? {
            val parent = element.parent
            if (element is XmlToken && parent is XmlText) {
                return element.text
            }
            //TODO 字符串的处理，需要把内置的 SkipAutopopupInStrings 的实现屏蔽掉才行
            if (element.elementType == ElementType.STRING_LITERAL && parent is PsiLiteralExpression) {
                return parent.value as? String
            }
            if (element.elementType == KtTokens.REGULAR_STRING_PART && parent is KtLiteralStringTemplateEntry) {
                return element.text
            }
            return null
        }
    }

}

