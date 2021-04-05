package xyz.mperminov.metalupcoming

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

@Parcelize
class AlbumInfo(
    val band: Band,
    val album: Album
) : Parcelable {

    fun matches(searchRequest: String): Boolean {
        return arrayOf(this.band.name, this.band.genre.value, this.album.title, this.album.date)
            .any { field -> field.contains(searchRequest, true) }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null) return false
        if (other !is AlbumInfo) return false
        return other.band.name == this.band.name && other.album.title == this.album.title
    }

    override fun hashCode(): Int = Objects.hash(this.band.name, this.album.title)
}

inline class Genre(val value: String)

@Parcelize
class Band(
    val name: String,
    val link: URL,
    val genre: Genre
) : Parcelable

@Parcelize
data class Album(
    val title: String,
    val link: URL,
    val type: TYPE,
    val date: String
) : Parcelable {

    fun parsedDate(): Date =
        try {
            requireNotNull(
                dateTimeFormatter.parse(
                    date.replace(
                        numericSuffixesRegex,
                        ""
                    )
                )
            )
        } catch (e: ParseException) {
            EPOCH_DATE
        }

    companion object DATE_FORMAT {
        internal val dateTimeFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        internal val numericSuffixesRegex = Regex("(?<=\\d)(st|nd|rd|th)")
        val EPOCH_DATE = Date(0)
    }

    enum class TYPE(internal val value: String) {
        EP("EP"),
        FULL_LENGTH("Full-length"),
        DEMO("Demo"),
        SINGLE("Single"),
        COMPILATION("Compilation"),
        SPLIT("Split"),
        LIVE_ALBUM("Live album"),
        COLLABORATION("Collaboration"),
        BOXED_SET("Boxed set"),
        UNKNOWN("");

        override fun toString(): String = value

        companion object {
            val values = values()
        }
    }
}

class AlbumTypeFactory {
    fun albumType(s: String): Album.TYPE {
        for (t in Album.TYPE.values) {
            if (t.value.equals(s, ignoreCase = true)) {
                return t
            }
        }
        return Album.TYPE.UNKNOWN
    }
}
