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

//TODO
//TODO 1. Упростить стейты
//TODO 2. Изменгить бекграунрд работу в соответствии с докладом
//TODO 3. Изменить на линеар лейаут
//TODO 4. Юай тесты
class AlbumsViewModel(
    private val okHttpClient: Lazy<OkHttpClient>,
    private val io: ExecutorService,
    private val handler: Handler,
    state: ParcelPropertiesMemento?
) : PersistableProperties, Closeable {

    val albums = AlbumInfoState(
        propertyOf(emptyList(), false),
        propertyOf(ListState.Empty, false),
        propertyOf("", false)
    )

    private val futures = mutableListOf<Future<*>>()

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
        futures.forEach { it.cancel(true) }
    }

    init {
        if (state !== null) state.restoreTo(this)
        loadAlbums()
    }

    fun loadAlbums() {
        albums._listState.value = ListState.Loading
        io.submit {
            try {
                val (count, firstList) = FetchAlbumsCount(okHttpClient.value).call()
                val firstListMapped = Callable { mapToAlbumList(firstList) }.call()
                val result = CopyOnWriteArrayList<AlbumInfo>()
                result.addAll(firstListMapped)
                handler.post { updateFirstResult(result, count) }

            } catch (e: Exception) {
                Log.e("Error", e.message.orEmpty())
                handler.post { setError(e) }
            }
        }.also { futures.add(it) }
    }

    private fun updateFirstResult(result: CopyOnWriteArrayList<AlbumInfo>, count: Int) {
        val tasks = mutableListOf<Runnable>()
        for (i in 100..count step 100) {
            tasks.add(
                FetchAlbumsFutureTask(
                    okHttpClient.value,
                    offset = i

                ) { offsetList ->
                    result.addAll(offsetList)
                    val sorted = result.sortedBy { it.album.parsedDate() }
                    handler.post { updateResult(sorted) }
                }
            )
        }
        tasks.forEach { task -> io.submit(task).also { future -> futures.add(future) } }
    }

    private fun setError(e: Exception) {
        Log.e("AlbumsViewModel", e.message ?: "")
        albums._listState.value = ListState.Error
    }

    private fun updateResult(list: List<AlbumInfo>) {
        albums.items.value = list
        albums._listState.value = ListState.Data
    }

    private companion object {
        val worker = WorkerOnExecutor(Executors.newSingleThreadExecutor())
    }
}

enum class ListState {
    Empty,
    Loading,
    Data,
    Error,
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
                    ListState.Data -> ListState.Empty
                    else -> originalState
                }
            } else {
                when (originalState) {
                    ListState.Empty -> ListState.Data
                    ListState.Error -> ListState.Data
                    else -> originalState
                }
            }
        }
    }
}
