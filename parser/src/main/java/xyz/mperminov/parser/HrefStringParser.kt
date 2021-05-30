package xyz.mperminov.parser

import java.net.URL

class HrefStringParser(
    private val hrefTextRegex: Regex,
    private val hrefLinkRegex: Regex
) {

    fun hrefText(hrefRawString: String): String {
        val results = hrefTextRegex.find(hrefRawString)
        return results?.groupValues?.get(1) ?: ""
    }

    fun link(hrefRawString: String): URL {
        val results = hrefLinkRegex.find(hrefRawString)
        val text = results?.groupValues?.get(1)?.trim('"') ?: ""
        return URL(text)
    }
}
