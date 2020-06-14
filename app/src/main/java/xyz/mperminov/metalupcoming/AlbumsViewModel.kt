package xyz.mperminov.metalupcoming

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.DiffUtil
import net.aquadc.persistence.android.parcel.ParcelPropertiesMemento
import net.aquadc.properties.MutableProperty
import net.aquadc.properties.diff.calculateDiffOn
import net.aquadc.properties.executor.WorkerOnExecutor
import net.aquadc.properties.flatMap
import net.aquadc.properties.map
import net.aquadc.properties.persistence.PropertyIo
import net.aquadc.properties.persistence.memento.PersistableProperties
import net.aquadc.properties.persistence.memento.restoreTo
import net.aquadc.properties.persistence.x
import net.aquadc.properties.propertyOf
import okhttp3.Call
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class AlbumsViewModel(
    private val okHttpClient: Lazy<OkHttpClient>,
    private val io: ExecutorService,
    state: ParcelPropertiesMemento?
) : PersistableProperties, Closeable {
    val albums = AlbumInfoState(
        propertyOf(emptyList(), true),
        propertyOf(ListState.Empty, true).apply {
            this.addChangeListener { old, new -> Log.d("ListState", "$old -> $new") }
        },
        propertyOf("", true)
    )

    private var loadingAlbumsInfo: Future<*>? = null

    val diffData = albums.filtered.calculateDiffOn(worker) { old, new ->
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
        io x albums.searchRequest
    }

    override fun close() {
        loadingAlbumsInfo?.cancel(true)
    }

    init {
        if (state !== null) state.restoreTo(this)
        loadAlbums()
    }

    fun loadAlbums() {
        loadingAlbumsInfo = io.submit {
            albums._listState.value = ListState.Loading
            try {
                val fetchingAlbums = CompletableFuture
                    .supplyAsync { FetchAlbumsCount(okHttpClient.value).call() }
                    .thenApplyAsync { count ->
                        val futures = mutableListOf<Future<List<AlbumInfo>>>()
                        for (i in 0..count step 100) {
                            futures.add(CompletableFuture
                                .supplyAsync {
                                    FetchAlbumsJsonArray(
                                        networkClient = okHttpClient.value,
                                        offset = i,
                                        length = 100
                                    ).call()
                                }
                                .thenApplyAsync { jsonArray ->
                                    MapJsonToAlbumInfoList(
                                        jsonArray
                                    ).call()
                                })
                        }
                        futures.map { it.get() }.flatten()
                    }
                albums.items.value = fetchingAlbums.get()
                albums._listState.value = ListState.Ok
            } catch (e: Exception) {
                Log.e("Execution", e.message ?: "")
                albums._listState.value = ListState.Error
            }
        }
    }

    @WorkerThread
    private fun fetchJson(
        okHttpClient: OkHttpClient,
        parser: HrefStringParser = HrefStringParser(
            RegexFactory().regex<String>(),
            RegexFactory().regex<Link>()
        )
    ): List<AlbumInfo> {

        val json = okHttpClient.prepareCall(0, 100)
            .execute()
            .unwrap()
            .string()

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
            albumInfo.distinct()
        } catch (e: Exception) {
            println(e.message)
            println(json)
            throw e
        }
    }

    private fun OkHttpClient.prepareCall(offset: Int, length: Int = 100): Call {
        return newCall(
            Request.Builder().get().url(
                "https://www.metal-archives.com/release/ajax-upcoming" +
                    "/json/1?sEcho=0&iDisplayStart=${offset}&iDisplayLength=${length}"
            )
                .build()
        )
    }


    private fun Response.unwrap(): ResponseBody =
        if (isSuccessful) body!!
        else throw IOException("HTTP $code")

    private companion object {
        val worker = WorkerOnExecutor(Executors.newSingleThreadExecutor())
    }
}

enum class ListState {
    Empty, Loading, Ok, Error
}

class AlbumInfoState(
    val items: MutableProperty<List<AlbumInfo>>,
    val _listState: MutableProperty<ListState>,
    val searchRequest: MutableProperty<String>
) {
    val filtered =
        items.flatMap { list ->
            searchRequest.map { s ->
                val filteredList = list.filter { it.matches(s) }
                filteredList
            }
        }

    val listState = filtered.flatMap { filtered: List<AlbumInfo> ->
        _listState.map { originalState ->
            if (filtered.isEmpty()) {
                when (originalState) {
                    ListState.Ok -> ListState.Empty
                    else -> originalState
                }
            } else {
                when (originalState) {
                    ListState.Empty -> ListState.Ok
                    ListState.Error -> ListState.Ok
                    else -> originalState
                }
            }
        }
    }
}


