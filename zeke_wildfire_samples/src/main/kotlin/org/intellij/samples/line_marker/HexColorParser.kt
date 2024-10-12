package org.intellij.samples.line_marker

import com.intellij.ui.ColorHexUtil
import com.intellij.ui.ColorUtil
import java.awt.Color
import java.util.regex.Pattern


/**
 * @param length 纯颜色字符（不带`#`、`0x`）的长度，
 * @param alphaAlignment 字符串中透明度所在的位置
 */
class HexColorParser @JvmOverloads constructor(
    private val length: Int,
    private val alphaAlignment: AlphaAlign = AlphaAlign.LEFT,
    private val mode: HexMode = HexMode.PLAIN
) : ColorParser {
    override fun parserColor(text: String): Color? {
        if (text.length < length) return null
        return try {
            val pattern: Pattern = when (mode) {
                HexMode.WITH_SHARP -> {
                    Pattern.compile("(#\\p{XDigit}{${length}}\\b)")
                }

                HexMode.WITH_0X -> {
                    Pattern.compile("(0x\\p{XDigit}{${length}}\\b)")
                }

                HexMode.PLAIN -> {
                    Pattern.compile("(\\p{XDigit}{${length}}\\b)")
                }
            }
            val matcher = pattern.matcher(text)
            if(matcher.matches()){
                var colorText: String
                matcher.toMatchResult().apply {
//                val range = TextRange(matchResult.start(), matchResult.end())
                    colorText = this.group()
                    if (alphaAlignment == AlphaAlign.LEFT) {
                        colorText = when (mode) {
                            HexMode.WITH_SHARP -> {
                                "${colorText.substring(3)}${colorText.substring(1, 3)}"
                            }

                            HexMode.WITH_0X -> {
                                "${colorText.substring(4)}${colorText.substring(2, 4)}"
                            }

                            HexMode.PLAIN -> {
                                "#${colorText.substring(2)}${colorText.substring(0, 2)}"
                            }
                        }
                    }
                }
                ColorHexUtil.fromHexOrNull(colorText)
            }else{
                return null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun convertColor(color: Color): String {
        var toHex = ColorUtil.toHex(color, alphaAlignment != AlphaAlign.NONE)
        if (alphaAlignment == AlphaAlign.LEFT) {
            val i = toHex.length - 2
            toHex = "${toHex.substring(i)}${toHex.substring(0, i)}"
        }
        return when (mode) {
            HexMode.WITH_SHARP -> {
                "#${toHex}"
            }

            HexMode.WITH_0X -> {
                "0x${toHex}"
            }

            HexMode.PLAIN -> {
                toHex
            }
        }

    }
}

enum class HexMode {
    /**
     * `#`号开始
     */
    WITH_SHARP,

    /**
     * `0x`开始
     */
    WITH_0X,

    /**
     * 无前缀
     */
    PLAIN
}

enum class AlphaAlign {
    LEFT,
    RIGHT,
    NONE
}