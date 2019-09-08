package xyz.mperminov.metalupcoming

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.album_layout.view.*
import xyz.mperminov.model.Album

class AlbumAdapter :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    var albums: List<Album> = listOf()

    class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun setData(albums: List<Album>) {
        this.albums = albums
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlbumViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.album_layout, null)
        return AlbumViewHolder(v)
    }


    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.itemView.title_view.text = albums[position].band
        holder.itemView.subtitle_view.text = albums[position].albumTitle
    }

    override fun getItemCount() = albums.size
}