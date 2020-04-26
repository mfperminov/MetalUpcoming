package xyz.mperminov.metalupcoming


import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.view
import splitties.views.dsl.recyclerview.recyclerView


@Suppress("UNCHECKED_CAST")
class MainActivity : InjectableActivity<AlbumsViewModel>() {

    @InternalSplittiesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Dark)
        setContentView(
            recyclerView {
                backgroundColor = getColorFromTheme(R.attr.listBackground)
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = object : RecyclerView.Adapter<AlbumHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
                        val cardView = view<CardView> {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(8.dp, 8.dp, 8.dp, 0)
                            }
                            setCardBackgroundColor(getColorFromTheme(R.attr.cardBackgroundColor))
                            preventCornerOverlap = true
                            radius = 8.toPx
                            elevation = 4.toPx
                            minimumHeight = 64.dp
                            addView(view<RelativeLayout>() {
                                addView(textView(id = GENRE_ID, theme = R.style.GenreTextStyle) {
                                    layoutParams = RelativeLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        setMargins(8.dp, 8.dp, 8.dp, 0)
                                        addRule(
                                            RelativeLayout.ALIGN_PARENT_START,
                                            RelativeLayout.TRUE
                                        )
                                        addRule(
                                            RelativeLayout.ALIGN_PARENT_TOP,
                                            RelativeLayout.TRUE
                                        )
                                        addRule(RelativeLayout.LEFT_OF, DATE_ID)
                                    }

                                })
                                addView(textView(id = DATE_ID, theme = R.style.DateTextStyle) {
                                    layoutParams = RelativeLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        setMargins(8.dp, 8.dp, 8.dp, 0)
                                        addRule(
                                            RelativeLayout.ALIGN_PARENT_END,
                                            RelativeLayout.TRUE
                                        )
                                        addRule(
                                            RelativeLayout.ALIGN_PARENT_TOP,
                                            RelativeLayout.TRUE
                                        )
                                    }
                                })
                                addView(textView(id = BAND_ID, theme = R.style.BandTextStyle) {
                                    layoutParams = RelativeLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        setMargins(8.dp, 8.dp, 8.dp, 0)
                                        addRule(
                                            RelativeLayout.ALIGN_PARENT_START,
                                            RelativeLayout.TRUE
                                        )
                                        addRule(
                                            RelativeLayout.BELOW,
                                            GENRE_ID
                                        )
                                    }
                                })
                                addView(
                                    textView(
                                        id = ALBUM_ID,
                                        theme = R.style.AlbumTitleTextStyle
                                    ) {
                                        layoutParams = RelativeLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        ).apply {
                                            setMargins(8.dp, 0, 8.dp, 0)
                                            addRule(
                                                RelativeLayout.ALIGN_PARENT_START,
                                                RelativeLayout.TRUE
                                            )
                                            addRule(
                                                RelativeLayout.BELOW,
                                                BAND_ID
                                            )
                                        }
                                    })
                                addView(textView(id = TYPE_ID, theme = R.style.AlbumTypeTextStyle) {
                                    layoutParams = RelativeLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        setMargins(8.dp, 8.dp, 8.dp, 8.dp)
                                        addRule(
                                            RelativeLayout.ALIGN_PARENT_START,
                                            RelativeLayout.TRUE
                                        )
                                        addRule(
                                            RelativeLayout.BELOW,
                                            ALBUM_ID
                                        )
                                    }
                                })
                            })
                        }
                        return AlbumHolder(cardView)
                    }

                    override fun getItemCount(): Int = vm.diffData.value.size

                    override fun onBindViewHolder(holder: AlbumHolder, position: Int) {
                        holder.bind(vm.diffData.value[position])
                    }

                    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                        vm.diffData.addChangeListener(onChange)
                    }

                    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
                        vm.diffData.removeChangeListener(onChange)
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
        fun bind(albumInfo: AlbumInfo) {
            cardView.findViewById<TextView>(GENRE_ID).text = albumInfo.band.genre.value
            cardView.findViewById<TextView>(DATE_ID).text = albumInfo.album.date
            cardView.findViewById<TextView>(BAND_ID).text = albumInfo.band.name
            cardView.findViewById<TextView>(ALBUM_ID).text = albumInfo.album.title
            cardView.findViewById<TextView>(TYPE_ID).text = albumInfo.album.type.value
        }
    }

    fun getColorFromTheme(id: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(id, typedValue, true)
        return typedValue.data
    }

    companion object {
        //card view ids
        @IdRes
        const val GENRE_ID = 1

        @IdRes
        const val DATE_ID = 2

        @IdRes
        const val BAND_ID = 3

        @IdRes
        const val ALBUM_ID = 4

        @IdRes
        const val TYPE_ID = 5
    }
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
val Int.toPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

