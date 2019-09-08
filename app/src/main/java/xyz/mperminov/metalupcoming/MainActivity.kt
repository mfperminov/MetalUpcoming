package xyz.mperminov.metalupcoming

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import xyz.mperminov.mapper.AlbumMapperJson
import xyz.mperminov.model.Album
import xyz.mperminov.network.RawDataRepositoryNetwork
import xyz.mperminov.network.RawDataRepositoryNetwork.Companion.BEGIN_PROGRESS
import xyz.mperminov.network.RawDataRepositoryNetwork.Companion.END_PROGRESS
import xyz.mperminov.network.RawDataRepositoryNetwork.Companion.ERROR
import xyz.mperminov.network.RawDataRepositoryNetwork.Companion.PROCEED_DATA
import xyz.mperminov.parser.HrefStringParser
import java.io.Serializable
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Suppress("UNCHECKED_CAST")
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val uiHandler = Handler { message ->
        when {
            message.what == ERROR -> Toast.makeText(
                this,
                (message.obj as Throwable).localizedMessage,
                Toast.LENGTH_LONG
            )
                .show()
            message.what == BEGIN_PROGRESS -> showProgress(true)
            message.what == END_PROGRESS -> showProgress(false)
            message.what == PROCEED_DATA -> {
                (viewAdapter as AlbumAdapter).setData(
                    AlbumMapperJson(HrefStringParser()).parse(
                        message.obj as String
                    )
                )
            }
        }

        true
    }

    private var needRequestData = false
    private val rawDataRepository = RawDataRepositoryNetwork(uiHandler)
    private val executor = Executors.newSingleThreadExecutor()
    private var dataFuture: Future<*>? = null
    private val dataTask = Runnable {
        try {
            rawDataRepository.getRawData()
            needRequestData = false
        } catch (e: InterruptedException) {
            needRequestData = true
            rawDataRepository.cancel()
        }
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewManager = LinearLayoutManager(this)
        viewAdapter = AlbumAdapter()
        recyclerView = findViewById<RecyclerView>(R.id.rv).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        if (savedInstanceState == null) {
            dataFuture = executor.submit(dataTask)
        } else {
            showProgress(false)
            savedInstanceState.getSerializable(ALBUM_LIST_KEY)?.let {
                val albumsList = it as List<Album>
                if (albumsList.isEmpty()) {
                    dataFuture = executor.submit(dataTask)
                } else {
                    (viewAdapter as AlbumAdapter).setData(
                        albumsList
                    )
                }
            }
        }
    }

    private fun showProgress(toShow: Boolean) {
        progressBar.visibility = if (toShow) View.VISIBLE else View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(
            ALBUM_LIST_KEY,
            (viewAdapter as AlbumAdapter).albums as Serializable
        )
    }

    override fun onStart() {
        super.onStart()
        if (needRequestData) {
            dataFuture = executor.submit(dataTask)
        }

    }

    override fun onStop() {
        super.onStop()
        dataFuture?.cancel(true)
        uiHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val ALBUM_LIST_KEY = "albumList"
    }
}
