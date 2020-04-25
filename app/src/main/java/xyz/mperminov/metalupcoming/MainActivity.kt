package xyz.mperminov.metalupcoming

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.aquadc.persistence.struct.Schema
import net.aquadc.persistence.type.byteString
import net.aquadc.persistence.type.collection
import net.aquadc.persistence.type.string
import net.aquadc.properties.android.bindings.view.bindVisibilityHardlyTo
import splitties.views.dsl.core.styles.AndroidStyles

@Suppress("UNCHECKED_CAST")
class MainActivity : InjectableActivity<AlbumsViewModel>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val androidStyles = AndroidStyles(this)
        val progressBar = androidStyles.progressBar
        setContentView(
            progressBar.default().also {

                it.bindVisibilityHardlyTo(vm.progress)
            }

//            this.recyclerView {
//            layoutManager = LinearLayoutManager(this@MainActivity)
//            adapter = object : RecyclerView.Adapter<AlbumHolder>() {
//                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
//                    return AlbumHolder(this@MainActivity.textView { })
//                }
//
//                override fun getItemCount(): Int = vm.diffData.value.size
//
//                override fun onBindViewHolder(holder: AlbumHolder, position: Int) {
//                    holder.bind(vm.diffData.value[position].band.name)
//                }
//
//                private var recyclers = 0
//                override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//                    if (recyclers++ == 0) vm.diffData.addChangeListener(onChange)
//                }
//
//                override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
//                    if (--recyclers == 0) vm.diffData.removeChangeListener(onChange)
//                }
//
//                private val onChange: (List<AlbumInfo>, List<AlbumInfo>, DiffUtil.DiffResult) -> Unit =
//                    { _, _, diff ->
//                        handler.post {
//                            diff.dispatchUpdatesTo(this)
//                        }
//                    }
//            }
//        }
        )
    }

    class AlbumHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(value: String) {
            textView.text = value
        }
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



