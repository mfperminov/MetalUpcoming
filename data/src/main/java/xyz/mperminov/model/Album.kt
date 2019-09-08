package xyz.mperminov.model

import java.io.Serializable
import java.net.URL

data class Album(
    val band: String,
    val bandLink: URL,
    val albumTitle: String,
    val albumLink: URL,
    val type: TYPE,
    val genre: String,
    val date: String
) : Serializable {
    enum class TYPE(private val value: String) {
        EP("EP"), FULL_LENGTH("Full-length"), DEMO("Demo"), SINGLE("Single"), COMPILATION("Compilation");

        override fun toString(): String = value

        companion object {
            fun fromString(s: String): TYPE {
                for (t in values()) {
                    if (t.value.equals(s, ignoreCase = true)) {
                        return t
                    }
                }
                throw IllegalArgumentException("No type with text $s found")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Album)
            this.band == other.band && this.albumTitle == other.albumTitle
        else false
    }

    companion object {
        val NONE = Album(
            "",
            URL("https://www.metal-archives.com/"),
            "",
            URL("https://www.metal-archives.com/"),
            TYPE.DEMO,
            "",
            ""
        )
    }
}