package xyz.mperminov.metalupcoming


import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.aquadc.persistence.struct.Schema
import net.aquadc.persistence.struct.Struct
import net.aquadc.persistence.struct.invoke
import net.aquadc.persistence.type.collection
import net.aquadc.persistence.type.string
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.styles.AndroidStyles
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.view
import splitties.views.dsl.recyclerview.recyclerView


@Suppress("UNCHECKED_CAST")
class MainActivity : InjectableActivity<AlbumsViewModel>() {


    @InternalSplittiesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val androidStyles = AndroidStyles(this)
        setTheme(R.style.Theme_Dark)

        val progressBar = androidStyles.progressBar
        setContentView(

            recyclerView {
                backgroundColor = Color.BLACK
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = object : RecyclerView.Adapter<AlbumHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
                        val cardView = view<CardView> {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(8.dp, 0, 8.dp, 8.dp)
                            }
                            setCardBackgroundColor(getColorFromTheme(android.R.attr.colorPrimaryDark))
                            preventCornerOverlap = true
                            radius = 8.toPx
                            elevation = 4.toPx
                            minimumHeight = 64.dp
                            addView(textView(id = 1))
                        }
                        return AlbumHolder(cardView)
                    }

                    override fun getItemCount(): Int = vm.diffData.value.size

                    override fun onBindViewHolder(holder: AlbumHolder, position: Int) {
                        holder.bind(vm.diffData.value[position].band.name)
                    }

                    private var recyclers = 0
                    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                        if (recyclers++ == 0) vm.diffData.addChangeListener(onChange)
                    }

                    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
                        if (--recyclers == 0) vm.diffData.removeChangeListener(onChange)
                    }

                    private val onChange: (List<AlbumInfo>, List<AlbumInfo>, DiffUtil.DiffResult) -> Unit =
                        { _, _, diff ->
                            handler.post {
                                diff.dispatchUpdatesTo(this)
                            }
                        }
                }
            }
        )
    }

    class AlbumHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView) {
        fun bind(value: String) {
            cardView.findViewById<TextView>(1).text = value
        }
    }

    fun getColorFromTheme(id: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(id, typedValue, true)
        return typedValue.data
    }
}

object AlbumInfoSchema : Schema<AlbumInfoSchema>() {
    val band = "band" let BandSchema
    val album = "album" let AlbumSchema
    val ListOf = collection(AlbumInfoSchema)
}

object BandSchema : Schema<BandSchema>() {
    val name = "name" let string
    val link = "link" let string
    val genre = "genre" let string
}

object AlbumSchema : Schema<AlbumSchema>() {
    val title = "name" let string
    val link = "link" let string
    val type = "genre" let string
    val date = "date" let string
}

fun AlbumInfo(bandStruct: Struct<BandSchema>, albumStruct: Struct<AlbumSchema>) =
    AlbumInfoSchema { s ->
        s[band] = bandStruct
        s[album] = albumStruct
    }

fun Band(band: Band) = BandSchema { s ->
    s[name] = band.name
    s[link] = band.link.toString()
    s[genre] = band.genre.value
}

fun Album(album: Album) = AlbumSchema { s ->
    s[title] = album.title
    s[link] = album.link.toString()
    s[type] = album.type.value
    s[date] = album.date
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
val Int.toPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

