package xyz.mperminov.parser

object RegexPatterns {
    val TEXT_INSIDE_HREF_TAG = "<a[^>]*>(.*?)</a>".toRegex()
    val LINK_INSIDE_HREF_TAG = "<a href=(.*?)>".toRegex()
}