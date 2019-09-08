package xyz.mperminov.parser

const val PARSE_ERROR = "PARSE ERROR"

class HrefStringParser {

    fun getTextInsideHrefTag(hrefStringToParse: String): String {
        val found = RegexPatterns.TEXT_INSIDE_HREF_TAG.find(hrefStringToParse)
        return if (found != null && found.groups[1] != null && found.groups[1]!!.value.isNotEmpty()) found.groups[1]!!.value else PARSE_ERROR
    }

    fun getLinkFromHrefTag(hrefStringToParse: String): String {
        val found = RegexPatterns.LINK_INSIDE_HREF_TAG.find(hrefStringToParse)
        return if (found != null && found.groups[1] != null && found.groups[1]!!.value.isNotEmpty()) found.groups[1]!!.value.replace(
            "\"",
            ""
        ) else PARSE_ERROR
    }
}