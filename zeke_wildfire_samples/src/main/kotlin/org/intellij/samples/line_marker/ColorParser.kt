package org.intellij.samples.line_marker

import java.awt.Color

interface ColorParser {

    fun parserColor(text: String): Color?

    fun convertColor(color: Color): String
}