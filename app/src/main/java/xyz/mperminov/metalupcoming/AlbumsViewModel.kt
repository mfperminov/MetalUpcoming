package xyz.mperminov.metalupcoming

import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.DiffUtil
import net.aquadc.persistence.android.parcel.ParcelPropertiesMemento
import net.aquadc.properties.diff.calculateDiffOn
import net.aquadc.properties.executor.WorkerOnExecutor
import net.aquadc.properties.persistence.PropertyIo
import net.aquadc.properties.persistence.memento.PersistableProperties
import net.aquadc.properties.propertyOf
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import xyz.mperminov.parser.HrefStringParser
import xyz.mperminov.parser.Link
import xyz.mperminov.parser.RegexFactory
import java.io.Closeable
import java.io.IOException
import java.util.LinkedList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AlbumsViewModel(
    private val okHttpClient: Lazy<OkHttpClient>,
    private val io: ExecutorService,
    state: ParcelPropertiesMemento?
) : PersistableProperties, Closeable {
    val albums = propertyOf(listOf<AlbumInfo>(), true)
    val listState = propertyOf(ListState.Empty, true).also {
        it.addChangeListener { old, new ->
            progress.value =
                new == ListState.Loading // trigger selectedItem and selectedItemPosition changes
        }
    }
    val progress = propertyOf(false, true)

    val diffData =
        albums.calculateDiffOn(worker) { old, new ->
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = old.size
                override fun getNewListSize(): Int = new.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    old[oldItemPosition] == new[newItemPosition]

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean =
                    old[oldItemPosition] == new[newItemPosition]
            })
        }

    override fun saveOrRestore(io: PropertyIo) {

    }

    override fun close() {
    }

    init {
        loadAlbums()
    }

    private fun loadAlbums() {
        io.submit {
            listState.value = ListState.Loading
            albums.value = okHttpClient.value.fetchJson()
            listState.value = ListState.Ok
        }
    }

    private companion object {
        val worker = WorkerOnExecutor(Executors.newSingleThreadExecutor())
    }
}

@WorkerThread
private fun OkHttpClient.fetchJson(
    parser: HrefStringParser = HrefStringParser(
        RegexFactory().regex<String>(),
        RegexFactory().regex<Link>()
    )
): List<AlbumInfo> {

    val json = newCall(
        Request.Builder().get().url("https://www.metal-archives.com/release/ajax-upcoming/json/1")
            .build()
    )
        .execute()
        .unwrap()
        .string()
        .replace("\"sEcho\": ,", "")

    return try {
        val obj = JSONObject(json)
        val mainArray = obj.getJSONArray("aaData")
        val albumInfo = LinkedList<AlbumInfo>()
        for (i in 0 until mainArray.length()) {
            val nextArr = mainArray.getJSONArray(i)
            val band = Band(
                parser.hrefText(nextArr.getString(0)),
                parser.link(nextArr.getString(0)),
                Genre(nextArr.getString(3))
            )
            val album = Album(
                parser.hrefText(nextArr.getString(1)),
                parser.link(nextArr.getString(1)),
                AlbumTypeFactory().albumType(nextArr.getString(2)),
                nextArr.getString(4)
            )
            albumInfo.add(AlbumInfo(band, album))
        }
        albumInfo
    } catch (e: Exception) {
        println(e.message)
        println(json)
        throw e
    }
}

enum class ListState {
    Empty, Loading, Ok, Error
}

private fun Response.unwrap(): ResponseBody =
    if (isSuccessful) body!!
    else throw IOException("HTTP $code")