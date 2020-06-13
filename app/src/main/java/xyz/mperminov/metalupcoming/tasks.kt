package xyz.mperminov.metalupcoming

import android.util.Log
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import xyz.mperminov.parser.HrefStringParser
import xyz.mperminov.parser.Link
import xyz.mperminov.parser.RegexFactory
import java.io.IOException
import java.util.LinkedList
import java.util.concurrent.Callable

const val BASE_URL = "https://www.metal-archives.com/release/ajax-upcoming" +
    "/json/1?sEcho=0&iDisplayStart=%d&iDisplayLength=%d"

class FetchAlbumsCount(
    private val networkClient: OkHttpClient,
    private val url: String = BASE_URL
) :
    Callable<Int> {
    override fun call(): Int {
        val json = networkClient.prepareCall(0, 100, url)
            .execute()
            .unwrap()
            .string()
        return try {
            val obj = JSONObject(json)
            return obj.getInt("iTotalRecords")
        } catch (e: Exception) {
            Log.e("FetchAlbumsTask", "${e.message}")
            0
        }
    }

    private fun OkHttpClient.prepareCall(offset: Int, length: Int = 100, url: String): Call {
        return newCall(
            Request.Builder().get().url(
                url.format(offset, length)
            )
                .build()
        )
    }

    private fun Response.unwrap(): ResponseBody =
        if (isSuccessful) body!!
        else throw IOException("HTTP $code")
}

class MapJsonToAlbumInfoList(
    private val jsonAlbumsArray: JSONArray,
    private val parser: HrefStringParser = HrefStringParser(
        RegexFactory().regex<String>(),
        RegexFactory().regex<Link>()
    )
) : Callable<List<AlbumInfo>> {
    override fun call(): List<AlbumInfo> {
        return try {
            val albumInfo = LinkedList<AlbumInfo>()
            for (i in 0 until jsonAlbumsArray.length()) {
                val nextArr = jsonAlbumsArray.getJSONArray(i)
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
            albumInfo.distinct()
        } catch (e: Exception) {
            Log.e("MapJsonToAlbumInfoList", e.message ?: "Unknown")
            Log.e("MapJsonToAlbumInfoList", jsonAlbumsArray.toString())
            emptyList()
        }
    }
}