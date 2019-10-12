package xyz.mperminov.metalupcoming

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.mperminov.model.Album

class AlbumAdapter :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    var albums: List<Album> = listOf()

    private val band: Int = 44
    private val album: Int = 45
    private val genre: Int = 46
    private val type: Int = 47
    private val date: Int = 48

    class AlbumViewHolder(
        view: View,
        val bandTextView: TextView,
        val albumTextView: TextView,
        val genreTextView: TextView,
        val typeTextView: TextView,
        val dateTextView: TextView
    ) : RecyclerView.ViewHolder(view)

    fun setData(albums: List<Album>) {
        this.albums = albums
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlbumViewHolder {
        val root = LinearLayout(parent.context)
        val rootLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(8.dp, 16.dp, 8.dp, 16.dp) }
        root.layoutParams = rootLayoutParams
        root.orientation = LinearLayout.VERTICAL
        val bandTextView = TextView(parent.context).apply {
            id = band
            setTextAppearance(context, R.style.TextAppearance_AppCompat_Subhead)
        }
        val albumTextView = TextView(parent.context).apply {
            id = album
            setTextAppearance(context, R.style.TextAppearance_AppCompat_Body2)
        }
        val genreTextView = TextView(parent.context).apply {
            id = genre
            setTextAppearance(context, R.style.TextAppearance_AppCompat_Body2)
        }
        val typeTextView = TextView(parent.context).apply {
            id = type
            setTextAppearance(context, R.style.TextAppearance_AppCompat_Body2)
        }
        val dateTextView = TextView(parent.context).apply {
            id = date
            setTextAppearance(context, R.style.TextAppearance_AppCompat_Body2)
        }
        root.addView(bandTextView, -1, -2)
        root.addView(albumTextView, -1, -2)
        root.addView(genreTextView, -1, -2)
        root.addView(typeTextView, -1, -2)
        root.addView(dateTextView, -1, -2)

        return AlbumViewHolder(
            root,
            bandTextView,
            albumTextView,
            genreTextView,
            typeTextView,
            dateTextView
        )
    }


    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bandTextView.text = albums[position].band
        holder.albumTextView.text = albums[position].albumTitle
        holder.genreTextView.text = albums[position].genre
        holder.typeTextView.text = albums[position].type.toString()
        holder.dateTextView.text = albums[position].date
    }

    override fun getItemCount() = albums.size
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()