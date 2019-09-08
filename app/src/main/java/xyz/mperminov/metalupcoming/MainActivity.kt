package xyz.mperminov.metalupcoming

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import xyz.mperminov.mapper.AlbumMapperJson
import xyz.mperminov.model.Album
import xyz.mperminov.network.RawDataRepositoryNetwork
import xyz.mperminov.parser.HrefStringParser
import java.io.Serializable

@Suppress("UNCHECKED_CAST")
class MainActivity : AppCompatActivity(R.layout.activity_main) {


    private val rawDataRepository = RawDataRepositoryNetwork(Handler { message ->
        when {
            message.what == ERROR -> Toast.makeText(
                this,
                (message.obj as Throwable).localizedMessage,
                Toast.LENGTH_LONG
            )
                .show()
            message.what == BEGIN_PROGRESS -> showProgress(true)
            message.what == END_PROGRESS -> showProgress(false)
            message.what == PROCEED_DATA -> (viewAdapter as AlbumAdapter).setData(
                AlbumMapperJson(HrefStringParser()).parse(
                    message.obj as String
                )
            )
        }

        true
    })
    private val handlerThread =
        HandlerThread("DataHandlerThread", Process.THREAD_PRIORITY_BACKGROUND).apply { start() }
    private val handler = Handler(handlerThread.looper)
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
            handler.post { rawDataRepository.getRawData() }
        } else {
            savedInstanceState.getSerializable(ALBUM_LIST_KEY)?.let {
                (viewAdapter as AlbumAdapter).setData(
                    it as List<Album>
                )
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

    override fun onStop() {
        super.onStop()
        handler.post { rawDataRepository.cancel() }
        handlerThread.quitSafely()
    }

    companion object {
        private const val ALBUM_LIST_KEY = "albumList"
        const val ERROR = 42
        const val END_PROGRESS = 40
        const val BEGIN_PROGRESS = 41
        const val PROCEED_DATA = 41
    }
}
