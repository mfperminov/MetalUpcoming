package xyz.mperminov.model

import android.os.Parcel
import android.os.Parcelable

data class Album(
    val band: String,
    val bandLink: String,
    val albumTitle: String,
    val albumLink: String,
    val type: TYPE,
    val genre: String,
    val date: String
) : Parcelable {
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

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        TYPE.values()[parcel.readInt()],
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun equals(other: Any?): Boolean {
        return if (other is Album)
            this.band == other.band && this.albumTitle == other.albumTitle
        else false
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(band)
        parcel.writeString(bandLink)
        parcel.writeString(albumTitle)
        parcel.writeString(albumLink)
        parcel.writeInt(type.ordinal)
        parcel.writeString(genre)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
        val NONE = Album(
            "",
            "https://www.metal-archives.com/",
            "",
            "https://www.metal-archives.com/",
            TYPE.DEMO,
            "",
            ""
        )
    }
}

