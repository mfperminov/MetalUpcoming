package xyz.mperminov.parser

import java.net.URI

class HrefStringParser(
    private val hrefTextRegex: Regex,
    private val hrefLinkRegex: Regex
) {

    fun hrefText(hrefRawString: String): String {
        val results = hrefTextRegex.find(hrefRawString)
        return results?.groupValues?.get(1) ?: ""
    }

    fun link(hrefRawString: String): Link {
        val results = hrefLinkRegex.find(hrefRawString)
        val text = results?.groupValues?.get(1)?.trim('"') ?: ""
        return if (text.isNotEmpty()) {
            Link(URI(text))
        } else {
            Link.EMPTY_LINK
        }
    }
}
