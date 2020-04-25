package xyz.mperminov.parser

class RegexFactory {
    inline fun <reified T> regex(): Regex {
        return when (T::class.java.name) {
            String::class.java.name -> "<a[^>]*>(.*?)</a>".toRegex()
            Link::class.java.name -> "<a href=(.*?)>".toRegex()
            else -> throw IllegalArgumentException()
        }
    }
}