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

    class AlbumViewHolder(
        view: View,
        val bandTextView: TextView,
        val albumTextView: TextView,
        val genreTextView: TextView
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
            tag = band
            setTextAppearance(context, R.style.TextAppearance_AppCompat_Subhead)
        }
        val albumTextView = TextView(parent.context).apply {
            id = album
            setTextAppearance(context, R.style.TextAppearance_AppCompat_Body2)
        }
        val genreTextView = TextView(parent.context).apply {
            tag = genre
            setTextAppearance(context, R.style.TextAppearance_AppCompat_Body1)
        }
        root.addView(bandTextView, -1, -2)
        root.addView(albumTextView, -1, -2)
        root.addView(genreTextView, -1, -2)

        return AlbumViewHolder(root, bandTextView, albumTextView, genreTextView)
    }


    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bandTextView.text = albums[position].band
        holder.albumTextView.text = albums[position].albumTitle
        holder.genreTextView.text = albums[position].genre
    }

    override fun getItemCount() = albums.size
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()