package xyz.mperminov.metalupcoming

import android.os.Handler
import android.util.Log
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
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

class AlbumsViewModel(
    private val okHttpClient: Lazy<OkHttpClient>,
    private val io: ExecutorService,
    private val handler: Handler,
    state: ParcelPropertiesMemento?
) : PersistableProperties, Closeable {

    val albums = AlbumInfoState(
        propertyOf(emptyList(), false),
        propertyOf(ListState.Empty, false).apply {
            this.addChangeListener { old, new -> Log.d("ListState", "$old -> $new") }
        },
        propertyOf("", false)
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

    fun loadAlbums(
        onResult: (partialResult: List<AlbumInfo>) -> Unit = { list ->
            handler.post {
                updateResult(
                    list
                )
            }
        },
        onFail: (e: Exception) -> Unit = { e ->
            handler.post {
                setError(e)
            }
        }
    ) {
        albums._listState.value = ListState.Loading
        loadingAlbumsInfo = io.submit {
            try {
                val (count, firstList) = FetchAlbumsCount(okHttpClient.value).call()
                val results = CopyOnWriteArrayList<List<AlbumInfo>>()
                val tasks = mutableListOf<Runnable>()
                tasks.add(object : FutureTask<List<AlbumInfo>>(
                    Callable { mapToAlbumList(firstList) }
                ) {
                    override fun done() {
                        try {
                            val value = get()
                            results.add(value)
                            onResult(results.toList().flatten().sortedBy { it.album.parsedDate() })
                        } catch (e: Exception) {
                            Log.e("AlbumsViewModel", e.javaClass.simpleName + e.localizedMessage)
                        }
                    }
                })
                for (i in 100..count step 100) {
                    val c: Runnable = object : FutureTask<List<AlbumInfo>>(
                        FetchAlbumsJsonArray(
                            okHttpClient.value,
                            offset = i
                        )
                    ) {
                        override fun done() {
                            try {
                                val value = get()
                                results.add(value)
                                onResult(
                                    results.toList().flatten().sortedBy { it.album.parsedDate() })
                            } catch (e: Exception) {
                                Log.e(
                                    "AlbumsViewModel",
                                    e.javaClass.simpleName + e.localizedMessage
                                )
                            }
                        }
                    }
                    tasks.add(c)
                }
                tasks.forEach { io.submit(it) }
            } catch (e: Exception) {
                onFail(e)
            }
        }
    }

    private fun setError(e: Exception) {
        Log.e("AlbumsViewModel", e.message ?: "")
        albums._listState.value = ListState.Error
    }

    private fun updateResult(list: List<AlbumInfo>) {
        albums.items.value = list
        albums._listState.value = ListState.Ok
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
