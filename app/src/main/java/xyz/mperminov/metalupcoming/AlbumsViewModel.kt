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
import okhttp3.OkHttpClient
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
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

    @WorkerThread
    fun loadAlbums() {
        loadingAlbumsInfo = io.submit {
            albums._listState.value = ListState.Loading
            try {
                val fetchingAlbums = CompletableFuture
                    .supplyAsync { FetchAlbumsCount(okHttpClient.value).call() }
                    .thenApplyAsync { fetchAlbumsCountResult ->
                        val count = fetchAlbumsCountResult.first
                        val firstHundred = fetchAlbumsCountResult.second
                        val futures = mutableListOf<CompletableFuture<List<AlbumInfo>>>().apply {
                            for (i in 100..count step 100) {
                                add(albumPageFetchFuture(i))
                            }
                        }
                        val unsortedList =
                            CopyOnWriteArrayList<AlbumInfo>(MapJsonToAlbumInfoList(firstHundred).call())
                        unsortedList.addAll(futures.map { f -> f.get() }.flatten())
                        if (unsortedList.all { it.album.parsedDate() != Album.DATE_FORMAT.EPOCH_DATE }) {
                            unsortedList.sortedBy { it.album.parsedDate().time }
                        } else {
                            unsortedList
                        }
                    }
                albums.items.value = fetchingAlbums.get()
                albums._listState.value = ListState.Ok
            } catch (e: Exception) {
                Log.e("AlbumsViewModel", e.message ?: "")
                albums._listState.value = ListState.Error
            }
        }
    }

    private fun albumPageFetchFuture(offset: Int): CompletableFuture<List<AlbumInfo>> =
        CompletableFuture
            .supplyAsync {
                FetchAlbumsJsonArray(
                    networkClient = okHttpClient.value,
                    offset = offset,
                    length = 100
                ).call()
            }
            .thenApplyAsync { jsonArray ->
                MapJsonToAlbumInfoList(
                    jsonArray
                ).call()
            }
            .exceptionally { t: Throwable? ->
                Log.e("AlbumPageFetch", "Failed for offset=$offset with cause: ${t?.message}")
                emptyList()
            }

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
