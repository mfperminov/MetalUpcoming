package xyz.mperminov.metalupcoming

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.aquadc.persistence.struct.Schema
import net.aquadc.persistence.type.byteString
import net.aquadc.persistence.type.collection
import net.aquadc.persistence.type.string
import net.aquadc.properties.diff.calculateDiffOn
import net.aquadc.properties.executor.WorkerOnExecutor
import net.aquadc.properties.propertyOf
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import splitties.views.dsl.core.textView
import splitties.views.dsl.recyclerview.recyclerView
import xyz.mperminov.parser.HrefStringParser
import xyz.mperminov.parser.Link
import xyz.mperminov.parser.RegexFactory
import java.io.IOException
import java.util.LinkedList
import java.util.concurrent.Executors

@Suppress("UNCHECKED_CAST")
class MainActivity : Activity() {
    val albums = propertyOf(listOf<AlbumInfo>(), true)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Executors.newSingleThreadExecutor().submit {
            val okHttpClient = OkHttpClient()
            albums.value = okHttpClient.fetchJson()
        }
        setContentView(this.recyclerView {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = object : RecyclerView.Adapter<AlbumHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
                    return AlbumHolder(this@MainActivity.textView { })
                }

                override fun getItemCount(): Int = diffData.value.size

                override fun onBindViewHolder(holder: AlbumHolder, position: Int) {
                    holder.bind(diffData.value[position].band.name)
                }

                private var recyclers = 0
                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                    if (recyclers++ == 0) diffData.addChangeListener(onChange)
                }

                override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
                    if (--recyclers == 0) diffData.removeChangeListener(onChange)
                }

                private val onChange: (List<AlbumInfo>, List<AlbumInfo>, DiffUtil.DiffResult) -> Unit =
                    { _, _, diff ->
                        handler.post {
                            diff.dispatchUpdatesTo(this)
                        }
                    }
            }
        })
    }

    class AlbumHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(value: String) {
            textView.text = value
        }
    }

    private companion object {
        val worker = WorkerOnExecutor(Executors.newSingleThreadExecutor())
    }
}

object AlbumInfoSchema : Schema<AlbumInfoSchema>() {
    val band = "band" let BandSchema
    val album = "album" let AlbumSchema
    val ListOf = collection(AlbumInfoSchema)
}

object BandSchema : Schema<BandSchema>() {
    val name = "name" let string
    val link = "link" let byteString
    val genre = "genre" let string
}

object AlbumSchema : Schema<BandSchema>() {
    val title = "name" let string
    val link = "link" let byteString
    val type = "genre" let byteString
    val date = "date" let string
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


private fun Response.unwrap(): ResponseBody =
    if (isSuccessful) body!!
    else throw IOException("HTTP $code")

