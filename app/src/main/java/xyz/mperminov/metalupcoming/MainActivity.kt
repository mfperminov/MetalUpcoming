package xyz.mperminov.metalupcoming

import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
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
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Suppress("UNCHECKED_CAST")
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val uiHandler = Handler { message ->
        when {
            message.what == ERROR -> {
                showErrorView(true)
                needRequestData = true
            }
            message.what == BEGIN_PROGRESS -> {
                showErrorView(false)
                showProgress(true)
            }
            message.what == END_PROGRESS -> showProgress(false)
            message.what == PROCEED_DATA -> {
                showErrorView(false)
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
            addItemDecoration(DividerItemDecoration(this@MainActivity, VERTICAL))
        }
        if (savedInstanceState == null) {
            dataFuture = executor.submit(dataTask)
        } else {
            showProgress(false)
            if (savedInstanceState.getBoolean(NEED_REQUEST_DATA)) {
                dataFuture = executor.submit(dataTask)
                return
            }
            savedInstanceState.getParcelableArrayList<Album>(ALBUM_LIST_KEY)?.let {
                val albumsList = it as List<Album>
                (viewAdapter as AlbumAdapter).setData(
                    albumsList
                )
            }
        }
    }

    private fun showProgress(toShow: Boolean) {
        progressBar.visibility = if (toShow) View.VISIBLE else View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(NEED_REQUEST_DATA, needRequestData)
        if ((viewAdapter as AlbumAdapter).albums.isNotEmpty())
            outState.putParcelableArrayList(
                ALBUM_LIST_KEY,
                (viewAdapter as AlbumAdapter).albums as ArrayList<Parcelable>
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

    private fun showErrorView(show: Boolean) {
        error_view.visibility = if (show) View.VISIBLE else View.INVISIBLE
        rv.visibility = if (show) View.INVISIBLE else View.VISIBLE
        findViewById<AppCompatButton>(R.id.refresh_button).setOnClickListener {
            dataFuture = executor.submit(dataTask)
        }
    }

    companion object {
        private const val ALBUM_LIST_KEY = "albumList"
        private const val NEED_REQUEST_DATA = "needRequestData"
    }
}