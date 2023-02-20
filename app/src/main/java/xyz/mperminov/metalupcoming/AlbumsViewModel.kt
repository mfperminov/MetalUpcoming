package xyz.mperminov.metalupcoming

import android.os.Handler
import android.util.Log
import net.aquadc.persistence.android.parcel.ParcelPropertiesMemento
import net.aquadc.properties.executor.WorkerOnExecutor
import net.aquadc.properties.mapWith
import net.aquadc.properties.persistence.PropertyIo
import net.aquadc.properties.persistence.memento.PersistableProperties
import net.aquadc.properties.persistence.memento.restoreTo
import net.aquadc.properties.persistence.x
import net.aquadc.properties.propertyOf
import okhttp3.OkHttpClient
import xyz.mperminov.metalupcoming.ViewListState.Data
import xyz.mperminov.metalupcoming.ViewListState.Empty
import xyz.mperminov.metalupcoming.ViewListState.Error
import java.io.Closeable
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

//TODO
//TODO 2. Изменгить бекграунрд работу в соответствии с докладом
//TODO 3. Изменить на линеар лейаут
//TODO 4. Юай тесты
class AlbumsViewModel(
    private val okHttpClient: Lazy<OkHttpClient>,
    private val io: ExecutorService,
    private val handler: Handler,
    state: ParcelPropertiesMemento?
) : PersistableProperties, Closeable {

    val searchRequest = propertyOf("")

    private val _state = propertyOf<ViewListState>(Empty)

    val state = _state.mapWith(searchRequest) { state, query ->
        if (state is Data) {
            Data(state.list.filter { it.matches(query) })
        } else {
            state
        }
    }

    val diffData get() = state

    private val futures = mutableListOf<Future<*>>()

    override fun saveOrRestore(io: PropertyIo) {
        io x searchRequest
    }

    override fun close() {
        futures.forEach { it.cancel(true) }
    }

    init {
        if (state !== null) state.restoreTo(this)
        loadAlbums()
    }

    fun loadAlbums() {
        _state.value = ViewListState.Loading
        io.submit {
            val result = ConcurrentSkipListSet<AlbumInfo> { first, second ->
                compareValuesBy(
                    first,
                    second
                ) { it.album.parsedDate }
            }
            try {
                val (count, firstList) = FetchAlbumsCount(okHttpClient.value).call()
                val firstListMapped = mapToAlbumList(firstList)
                result.addAll(firstListMapped)
                handler.post { updateResult(result.toList()) }
                updateFirstResult(result, count)
            } catch (e: Exception) {
                Log.e("Error", e.message.orEmpty())
                handler.post { setError(e) }
            }
        }.also { futures.add(it) }
    }

    private fun updateFirstResult(result: ConcurrentSkipListSet<AlbumInfo>, count: Int) {
        val tasks = mutableListOf<Runnable>()
        for (i in FIRST_OFFSET..count step ALBUMS_IN_REQUEST_COUNT) {
            tasks.add(
                FetchAlbumsFutureTask(
                    okHttpClient.value,
                    offset = i
                ) { offsetList ->
                    result.addAll(offsetList)
                    val list = result.toList()
                    handler.post { updateResult(list) }
                }
            )
        }
        tasks.forEach { task -> io.submit(task).also { future -> futures.add(future) } }
    }

    private fun setError(e: Exception) {
        Log.e("AlbumsViewModel", e.message ?: "")
        _state.value = Error(e)
    }

    private fun updateResult(list: List<AlbumInfo>) {
        _state.value = Data(list)
    }

    private companion object {
        val worker = WorkerOnExecutor(Executors.newSingleThreadExecutor())
        const val ALBUMS_IN_REQUEST_COUNT = 100
        const val FIRST_OFFSET = 100
    }
}

sealed class ViewListState(val list: List<AlbumInfo>) {
    object Empty : ViewListState(emptyList())
    object Loading : ViewListState(emptyList())
    class Data(list: List<AlbumInfo>) : ViewListState(list)
    class Error(val e: Throwable) : ViewListState(emptyList())
}
