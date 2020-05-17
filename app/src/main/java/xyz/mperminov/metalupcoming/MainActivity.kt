package xyz.mperminov.metalupcoming


import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import net.aquadc.properties.android.bindings.view.bindVisibilitySoftlyTo
import net.aquadc.properties.map
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.appcompat.toolbar
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.material.appBarLayout
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.gravityBottom
import splitties.views.gravityEnd


@Suppress("UNCHECKED_CAST")
class MainActivity : InjectableActivity<AlbumsViewModel>() {

    private var handler: Handler? = Handler(Looper.getMainLooper())

    @InternalSplittiesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null)
            setTheme(R.style.Theme_Dark) else setTheme(R.style.Theme_Light)
        val rootView = view<CoordinatorLayout> {
            backgroundColor = getColorFromTheme(R.attr.listBackground)
            addView(
                appBarLayout {
                    layoutParams = CoordinatorLayout.LayoutParams(matchParent, wrapContent).apply {
                        gravity = gravityBottom or gravityEnd
                        behavior = HideBottomViewOnScrollBehavior<AppBarLayout>()
                    }

                    addView(toolbar {
                        setTitle(R.string.app_name)
                        backgroundColor = getColorFromTheme(R.attr.toolbarColor)
                        layoutParams = AppBarLayout.LayoutParams(matchParent, wrapContent).apply {
                            scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                        }
                        setNavigationIcon(R.drawable.ic_settings_brightness_24px)
                        setNavigationOnClickListener { flipTheme() }
                        addView(view<SearchView> {
                            layoutDirection = View.LAYOUT_DIRECTION_RTL
                            layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
                            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    query?.let { vm.albums.searchRequest.value = it }
                                    return true
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    newText?.let { vm.albums.searchRequest.value = it }
                                    return true
                                }
                            })
                        })
                    })
                })
            addView(recyclerView(id = 8) {
                bindVisibilitySoftlyTo(vm.albums.listState.map { it == ListState.Ok })
                backgroundColor = getColorFromTheme(R.attr.listBackground)
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = object : RecyclerView.Adapter<AlbumHolder>() {

                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
                        val cardView = cardview()
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
                            this@MainActivity.handler?.post {
                                diff.dispatchUpdatesTo(this)
                            }
                        }
                }
            }
            )
            addView(this@MainActivity.emptyView().apply {
                bindVisibilitySoftlyTo(vm.albums.listState.map { it == ListState.Empty })
            })
            addView(this@MainActivity.errorView { vm.loadAlbums() }.apply {
                bindVisibilitySoftlyTo(vm.albums.listState.map { it == ListState.Error })
            })
        }

        setContentView(rootView)
    }

    private fun flipTheme() {
        recreate()
    }

    override fun onDestroy() {
        handler?.removeCallbacksAndMessages(null)
        findViewById<RecyclerView>(8).adapter = null
        super.onDestroy()
    }

    @InternalSplittiesApi
    private fun RecyclerView.cardview(): CardView {
        return view<CardView> {
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

    @SuppressLint("ResourceType")
    companion object {
        //card view ids
        const val GENRE_ID = 1
        const val DATE_ID = 2
        const val BAND_ID = 3
        const val ALBUM_ID = 4
        const val TYPE_ID = 5
    }
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
val Int.toPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

