package xyz.ezsky.ybutools.data.jsxt.tools

class QzBrParser(source: String) : QzParser(source) {

    override fun parseCourseName(infoStr: String): String {
        return infoStr.substringBefore("<br>").trim()
    }

}