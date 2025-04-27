package com.group15.nltouml.util

fun titlecase(str: String): String {
    return titlecase(str, null)
}

fun titlecase(str: String, delimiters: CharArray?): String {
    val delimLen = (delimiters?.size ?: -1)
    if (str.isEmpty() || delimLen == 0) {
        return str
    }
    val strLen = str.length
    val buffer = StringBuffer(strLen)
    var capitalizeNext = true
    for (i in 0 until strLen) {
        val ch = str[i]

        if (isDelimiter(ch, delimiters)) {
            buffer.append(ch)
            capitalizeNext = true
        } else if (capitalizeNext) {
            buffer.append(ch.titlecaseChar())
            capitalizeNext = false
        } else {
            buffer.append(ch)
        }
    }
    return buffer.toString()
}

private fun isDelimiter(ch: Char, delimiters: CharArray?): Boolean {
    if (delimiters == null) {
        return Character.isWhitespace(ch)
    }
    var i = 0
    val isize = delimiters.size
    while (i < isize) {
        if (ch == delimiters[i]) {
            return true
        }
        i++
    }
    return false
}