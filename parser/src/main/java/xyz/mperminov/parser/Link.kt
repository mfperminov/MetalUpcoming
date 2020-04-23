package xyz.mperminov.parser

import java.net.URI

class Link(private val link: URI) {
    override fun toString(): String {
        return link.toString()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? Link)?.link?.equals(link) ?: false
    }

    companion object {
        val EMPTY_LINK = Link(URI(""))
    }
}