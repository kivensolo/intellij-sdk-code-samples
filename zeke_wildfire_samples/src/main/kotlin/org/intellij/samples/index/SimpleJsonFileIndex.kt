package org.intellij.samples.index

import com.intellij.json.JsonElementTypes
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLexer
import com.intellij.json.json5.Json5FileType
import com.intellij.json.json5.Json5Lexer
import com.intellij.lexer.Lexer
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.IOUtil
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput


private val NAME = ID.create<String, String>("org.intellij.samples.index.SimpleJsonFileIndex")

/**
 * 简单的json件索引,
 * 暂时没用。部分代码留着。
 */
class SimpleJsonFileIndex : FileBasedIndexExtension<String, String>() {

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
        return DefaultFileTypeSpecificInputFilter(JsonFileType.INSTANCE)
    }

    override fun getVersion(): Int = 0

    override fun dependsOnFileContent(): Boolean = true


    /**
     * 自定义的数据索引器 <key,value,Data>
     */
    object MyDataIndexer : DataIndexer<String, String, FileContent> {
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
            val isJsonFIleType = fileType is JsonFileType

            //Json的词法分析器
            var lexer = if(fileType == Json5FileType.INSTANCE){
                 Json5Lexer()
            }else{
                JsonLexer()
            }

            //先做一个最简单的映射关联
            val jsonContent = fileContent.contentAsText.toString()

            // 索引的映射map
            val map = HashMap<String, String>()
            lexer.start(jsonContent) //开始分析json数据

            var idFound = false
            var nesting = 0
            while (!idFound) {
                val token: IElementType? = lexer.tokenType
                // Nesting level can only change at curly braces.
                if (token === JsonElementTypes.L_CURLY) { // {
                    nesting++
                } else if (token === JsonElementTypes.R_CURLY) { // }
                    nesting--
                }
                when (lexer.tokenText){
                    "KingZ" -> {
                        val currentPosition = lexer.currentPosition
                        val offset = currentPosition.offset
                        idFound = true
                        map["KingZ"] = lexer.tokenText + ":" + offset
                    }
                }
                lexer.advance()
            }
            //TODO 索引有了，如何用？？
            return map
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

    private fun skipWhitespacesAndGetTokenType(lexer: Lexer): IElementType? {
        while (lexer.tokenType === TokenType.WHITE_SPACE ||
            lexer.tokenType === JsonElementTypes.LINE_COMMENT ||
            lexer.tokenType === JsonElementTypes.BLOCK_COMMENT) {
            lexer.advance()
        }
        return lexer.tokenType
    }

}