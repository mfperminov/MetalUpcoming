package xyz.mperminov.metalupcoming

import android.os.Parcel
import android.os.Parcelable
import xyz.mperminov.parser.Link
import java.net.URI
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlbumInfo(
    val band: Band,
    val album: Album
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable<Band>(Band::class.java.classLoader) as Band,
        parcel.readParcelable<Album>(Album::class.java.classLoader) as Album
    )

    fun matches(searchRequest: String): Boolean {
        return arrayOf(this.band.name, this.band.genre.value, this.album.title, this.album.date)
            .any { field -> field.contains(searchRequest, true) }
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(band, flags)
        parcel.writeParcelable(album, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is AlbumInfo) return false
        if (other === this) return true
        return other.band.name == this.band.name && other.album.title == this.album.title
    }

    override fun hashCode(): Int {
        var result = 17
        result *= this.band.name.hashCode()
        result *= this.album.title.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
    }
}

inline class Genre(val value: String)

class Band(
    val name: String,
    val link: Link,
    val genre: Genre
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        Link(parcel.readSerializable() as URI),
        Genre(parcel.readString()!!)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeSerializable(link.uri)
        parcel.writeString(genre.value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Band> {
        override fun createFromParcel(parcel: Parcel): Band {
            return Band(parcel)
        }

        override fun newArray(size: Int): Array<Band?> {
            return arrayOfNulls(size)
        }
    }
}

class Album(
    val title: String,
    val link: Link,
    val type: TYPE,
    val date: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        Link(parcel.readSerializable() as URI),
        AlbumTypeFactory().albumType(parcel.readString()!!),
        parcel.readString()!!
    )

    fun parsedDate(): Date =
        try {
            requireNotNull(
                DATE_FORMAT.dateTimeFormatter.parse(
                    date.replace(
                        DATE_FORMAT.numericSuffixesRegex,
                        ""
                    )
                )
            )
        } catch (e: ParseException) {
            DATE_FORMAT.EPOCH_DATE
        }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeSerializable(link.uri)
        parcel.writeString(type.toString())
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    object DATE_FORMAT {
        internal val dateTimeFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        internal val numericSuffixesRegex = Regex("(?<=\\d)(st|nd|rd|th)")
        val EPOCH_DATE = Date(0)
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
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
