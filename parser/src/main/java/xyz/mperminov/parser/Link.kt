package xyz.mperminov.parser

import java.net.URI

class Link(val uri: URI) {
    override fun toString(): String {
        return uri.toString()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? Link)?.uri?.equals(uri) ?: false
    }

    override fun hashCode(): Int {
        return uri.hashCode()
    }

    companion object {
        val EMPTY_LINK = Link(URI(""))
    }
}