package xyz.mperminov.metalupcoming


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import net.aquadc.properties.android.bindings.view.bindVisibilitySoftlyTo
import net.aquadc.properties.map
import splitties.views.backgroundColor
import splitties.views.dsl.appcompat.toolbar
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.material.appBarLayout
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.gravityBottom
import splitties.views.gravityEnd
import java.net.URL

@Suppress("UNCHECKED_CAST")
class MainActivity : InjectableActivity<AlbumsViewModel>() {

    private val handler = Handler(Looper.getMainLooper())
    private val onClick: (AlbumInfo) -> Unit = { info ->
        AlertDialog.Builder(this, R.style.AlertDialogCustom).setItems(
            R.array.MA_destinations
        ) { dialog, which ->
            when (which) {
                0 -> openPage(info.album.link)
                1 -> openPage(info.band.link)
            }
            dialog.dismiss()
        }
            .setTitle(R.string.dialog_to_pages_title)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleTheme()
        val rootView = CoordinatorLayout(this).apply {
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
                        addView(SearchView(this@MainActivity).apply {
                            id = SEARCH_VIEW_ID
                            imeOptions =
                                EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_FULLSCREEN or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                            queryHint = getString(R.string.search_hint)
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
            addView(recyclerView(id = RECYCLER_VIEW_ID) {
                bindVisibilitySoftlyTo(vm.albums.listState.map { it == ListState.Data })
                clipToPadding = false
                setPadding(0, 0, 0, 4.dp)
                backgroundColor = getColorFromTheme(R.attr.toolbarColor)
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = object : RecyclerView.Adapter<AlbumHolder>() {

                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
                        val cardView = itemView()
                        return AlbumHolder(cardView, onClick)
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
                            this@MainActivity.handler.post {
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
            addView(this@MainActivity.progressView().apply {
                bindVisibilitySoftlyTo(vm.albums.listState.map { it == ListState.Loading })
            })
        }
        setContentView(rootView)
        if (savedInstanceState != null) {
            val searchView = findViewById<SearchView>(SEARCH_VIEW_ID)
            if (searchView.query.isNotEmpty()) {
                searchView.isIconified = false
            }
        }
    }

    private fun handleTheme() {
        val theme: Theme? = getThemeFromSettings()
        if (theme == null) {
            if (this.isDarkSystemThemeOn()) {
                setTheme(R.style.Theme_Dark)
                saveTheme(Theme.DARK)
            } else {
                setTheme(R.style.Theme_Light)
                saveTheme(Theme.LIGHT)
            }
        } else {
            when (theme) {
                Theme.LIGHT -> setTheme(R.style.Theme_Light)
                Theme.DARK -> setTheme(R.style.Theme_Dark)
            }
        }
    }

    private fun saveTheme(theme: Theme) {
        this.getPreferences(Context.MODE_PRIVATE).edit().putString(THEME_KEY, theme.toString())
            .apply()
    }

    private fun getThemeFromSettings(): Theme? {
        val s = this.getPreferences(Context.MODE_PRIVATE).getString(THEME_KEY, null)
        return if (s != null) Theme.valueOf(s) else null
    }

    private fun flipTheme() {
        when (requireNotNull(getThemeFromSettings())) {
            Theme.LIGHT -> saveTheme(Theme.DARK)
            Theme.DARK -> saveTheme(Theme.LIGHT)
        }
        recreate()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        findViewById<RecyclerView>(RECYCLER_VIEW_ID).adapter = null
        super.onDestroy()
    }

    private fun itemView(): CardView {
        return CardView(this@MainActivity).apply {
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
            addView(RelativeLayout(this@MainActivity).apply {
                addView(
                    textView(
                        id = GENRE_ID,
                        theme = R.style.GenreTextStyle
                    ) {
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
                            addRule(
                                RelativeLayout.LEFT_OF,
                                DATE_ID
                            )
                        }

                    })
                addView(
                    textView(
                        id = DATE_ID,
                        theme = R.style.DateTextStyle
                    ) {
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
                addView(
                    textView(
                        id = BAND_ID,
                        theme = R.style.BandTextStyle
                    ) {
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
                addView(
                    textView(
                        id = TYPE_ID,
                        theme = R.style.AlbumTypeTextStyle
                    ) {
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

    class AlbumHolder(
        private val cardView: CardView,
        private val onClickListener: (AlbumInfo) -> Unit
    ) :
        RecyclerView.ViewHolder(cardView) {
        fun bind(albumInfo: AlbumInfo) {
            cardView.findViewById<TextView>(GENRE_ID).text = albumInfo.band.genre.value
            cardView.findViewById<TextView>(DATE_ID).text = albumInfo.album.date
            cardView.findViewById<TextView>(BAND_ID).text = albumInfo.band.name
            cardView.findViewById<TextView>(ALBUM_ID).text = albumInfo.album.title
            cardView.findViewById<TextView>(TYPE_ID).text = albumInfo.album.type.value

            cardView.setOnClickListener {
                onClickListener.invoke(albumInfo)
            }
        }
    }

    private fun openPage(url: URL) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url.toString())
            )
        )
    }

    @SuppressLint("ResourceType")
    companion object {
        //card view ids
        const val GENRE_ID = 1
        const val DATE_ID = 2
        const val BAND_ID = 3
        const val ALBUM_ID = 4
        const val TYPE_ID = 5
        const val RECYCLER_VIEW_ID = 8
        private const val SEARCH_VIEW_ID = 9
        private const val THEME_KEY = "THEME_KEY"
    }
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
val Int.toPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

