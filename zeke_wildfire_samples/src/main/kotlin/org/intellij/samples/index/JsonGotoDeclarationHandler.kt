package org.intellij.samples.index

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.json.psi.JsonElement
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.util.containers.toArray
import org.jetbrains.android.facet.AndroidFacet


/**
 * 导航实现
 * 创建一个导航处理器，目的是当用户点击 JSON 文件中的目标字符串时，如果目标字符串符合
 * [SimpleJavaMethodFileIndex]内部判断逻辑，则会返回解析后的目标Psi元素集合。
 * IDE会自己弹出提示, 导航到相应的 Java 方法。
 *
 * 这个导航如何和json关联？？？ ----- PSI解析器
 * 而目标字符串，有需要编写一个PSI解析器来解析JSon文件并找到字符串字面量。
 * 这个解析器IDEA已经内置了，参考[com.intellij.json.JsonParserDefinition]
 */
class JsonGotoDeclarationHandler: GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if(sourceElement == null) return null

        if(sourceElement !is JsonElement){
            //检测这个元素是不是Json的Element,如果不是，就return
            return null
        }
        val file: PsiFile = sourceElement.containingFile
        println("元素所在的psi文件为：${file.name}")

        val instance:AndroidFacet? = AndroidFacet.getInstance(sourceElement)
        if(instance == null){
            println("元素无法创建出AndroidFacet")
        }

        val text = sourceElement.text
        var methodTargetName = ""
        if(text.startsWith("\"") and  text.endsWith("\"")){
            methodTargetName = text.substring(1, text.length - 1)
        }
        if(methodTargetName == ""){
            return null
        }
        val project: Project = sourceElement.project
        val methods: Collection<PsiMethod>? = SimpleJavaMethodFileIndex.getMethodsPsiByName(project, methodTargetName)

        //将找到的目标方法转换为PsiElement数组并返回
        return methods?.toArray(PsiElement.EMPTY_ARRAY)
    }

    override fun getActionText(context: DataContext): String? {
        return "Go to Java Method"
    }
}