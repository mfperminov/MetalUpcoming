package xyz.mperminov.parser

object RegexPatterns {
    val TEXT_INSIDE_HREF_TAG = "<a[^>]*>(.*?)</a>".toRegex()
    val LINK_INSIDE_HREF_TAG = "<a href=(.*?)>".toRegex()
}

class RegexFactory {
    inline fun <reified T> regex(): Regex {
        return when (T::class.java.name) {
            String::class.java.name -> "<a[^>]*>(.*?)</a>".toRegex()
            Link::class.java.name -> "<a href=(.*?)>".toRegex()
            else -> throw IllegalArgumentException()
        }
    }
}