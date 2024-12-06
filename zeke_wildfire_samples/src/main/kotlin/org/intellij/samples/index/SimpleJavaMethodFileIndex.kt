package org.intellij.samples.index

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.IOUtil
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput
import java.util.stream.Collectors
import java.util.stream.Stream


private val NAME = ID.create<String, String>("org.intellij.samples.index.SimpleJavaMethodFileIndex")

/**
 * 简单的json件索引,
 */
open class SimpleJavaMethodFileIndex : FileBasedIndexExtension<String, String>() {

    private val myDataIndexer: MyDataIndexer = MyDataIndexer
    private val myDataExternalizer: MyDataExternalizer = MyDataExternalizer
    /**
     * 返回唯一的索引ID
     */
    override fun getName(): ID<String, String> = NAME

    /**
     * 返回DataIndex
     * 负责根据文件内容构建索引
     */
    override fun getIndexer(): DataIndexer<String, String, FileContent> {
        return myDataIndexer
    }

    /**
     * 返回负责比较键并以序列化二进制格式存储它们的 KeyDescriptor
     * 默认用 EnumeratorStringDescriptor.INSTANCE 即可
     */
    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    override fun getValueExternalizer(): DataExternalizer<String> = myDataExternalizer

    /**
     * 可以使用 DefaultFileTypeSpecificInputFilter 过滤自己需要的文件
     */
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return DefaultFileTypeSpecificInputFilter(JavaFileType.INSTANCE)
    }

    override fun getVersion(): Int = 0

    override fun dependsOnFileContent(): Boolean = true


    /**
     * 自定义的数据索引器 <key,value,Data>
     */
    object MyDataIndexer : DataIndexer<String, String, FileContent> {
        var fileName = ""

        /**
         * 存储从文件内容中提取的关键信息，并返回。这些信息可以在后续的查询中被快速访问。
         * 通过预处理文件内容并将其转换为索引数据，也可以在后续的查询中避免重复解析文件，从而提高性能。
         *
         * @param FileContent fileContent 文件内容对象
         *          通过 `fileContent.contentAsText.toString()` 可以获取到文件的纯文本数据。
         *
         * @return Map  包含有索引信息的Map
         * 键 (Key)：
         *      类型：通常是一个 String 或其他不可变对象。
         *      要求：键必须是唯一的，以便能够唯一标识索引中的条目。
         *      键的选择应根据具体需求来确定，例如文件路径、类名、方法名等。
         * 值 (Value)：
         *      类型：可以是任何类型的对象，但通常是简单的数据结构，如 String、Integer、List 等。
         *      要求：值应该包含需要在索引中存储的信息。
         *      例如，如果正在索引某个文件中的所有类名，那么值可以是一个 List<String>，其中每个字符串都是一个类名。
         *      这样，后续的查询就可以通过键来获取到这些类名，而无需再次解析文件。
         * FIXME 如何调用？？ 调用几次
         */
        override fun map(fileContent: FileContent): Map<String, String> {
            val fileType = fileContent.fileType
            val psiFile: PsiFile = fileContent.psiFile
            if (psiFile !is PsiJavaFile) {
                return emptyMap()
            }
            val packageName = psiFile.packageName
            if(!packageName.startsWith("com.zeke")){
                //做文件包路径过滤，防止对所有java代码做处理
                return emptyMap()
            }
            val fileFullName = psiFile.name
            val lastDotIndex = fileFullName.lastIndexOf(".")
            if (lastDotIndex != -1) {
                fileName = fileFullName.substring(0, lastDotIndex)
            }else{
                return emptyMap()
            }
            val indexMap: MutableMap<String, String> = HashMap()
            collectMethodNames(psiFile, indexMap)
            return indexMap
        }


        private fun collectMethodNames(element: PsiElement, indexMap: MutableMap<String, String>) {
            var className = ""
            if(element is PsiClass){
                className = element.name.toString()
            }
            //获取所有的方法psi元素
            val methods = PsiTreeUtil.getChildrenOfType(element, PsiMethod::class.java)
            if (methods != null) {
                for (method in methods) {
                    val key = fileName + "#" + className + "#" + method.name
                    indexMap[key] = "unUse"
                }
            }
            val classes = PsiTreeUtil.getChildrenOfType(element, PsiClass::class.java)
            if (classes != null) {
                for (clazz in classes) {
                    collectMethodNames(clazz, indexMap)
                }
            }
        }
    }


    object MyDataExternalizer : DataExternalizer<String> {
        override fun save(out: DataOutput, value: String?) {
            if(value != null){
                IOUtil.writeUTF(out, value)
            }
        }

        override fun read(`in`: DataInput): String {
            return IOUtil.readUTF(`in`)
        }
    }


    companion object{
        /**
         * 通过FileBasedIndex获取到目标索引找到虚拟文件集合，
         * 同时对虚拟文件做处理，筛选出符合条件的psiMethod集合。
         * @param project Project
         * @param valueContent String  目标方法名，格式为： 文件名#类名#....#方法名
         */
        @JvmStatic
        fun getMethodsPsiByName(project: Project, valueContent: String): Collection<PsiMethod>? {
            val fileIndex = FileBasedIndex.getInstance()

            /**
             * 从给定的项目中获取包含特定名称和方法名的文件列表
             * NAME：要查找的索引名称。
             * methodName：要查找的方法名。
             * GlobalSearchScope.allScope(project)：全局搜索范围，指定在当前项目中进行搜索。
             */
            val containingFiles:Collection<VirtualFile> = fileIndex.getContainingFiles(NAME, valueContent, GlobalSearchScope.allScope(project))

            val segments = valueContent.split("#")
            val fileName = segments[0]
            val methodName = segments[segments.size - 1]

            return containingFiles.stream()
                .map {virtualFile: VirtualFile ->
                    //将每个 VirtualFile 转换为 PsiFile 对象
                    PsiManager.getInstance(project).findFile(virtualFile)
                }
                .flatMap { psiFile: PsiFile? ->
                    if(psiFile != null){
                        val fName = psiFile.name.split(".")[0]
                        if(fName != fileName){
                            return@flatMap Stream.empty()
                        }
                    }
                    //在每个 PsiFile 中查找所有的 PsiMethod 对象
                    PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod::class.java ).stream()
                }
                .filter { method: PsiMethod ->
                    methodName == method.name
                }
                // 将过滤后的 PsiMethod 对象收集到一个列表中并返回
                .collect(Collectors.toList())
        }
    }

}