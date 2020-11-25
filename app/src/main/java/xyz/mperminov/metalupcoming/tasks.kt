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

private const val BASE_URL = "https://www.metal-archives.com/release/ajax-upcoming" +
    "/json/1?sEcho=0&iDisplayStart=%d&iDisplayLength=%d"

class FetchAlbumsCount(
    private val networkClient: OkHttpClient,
    private val url: String = BASE_URL
) :
    Callable<Pair<Int, JSONArray>> {
    override fun call(): Pair<Int, JSONArray> {
        val json = networkClient.prepareCall(0, 100, url)
            .execute()
            .unwrap()
            .string()
        return try {
            val obj = JSONObject(json)
            obj.getInt("iTotalRecords") to obj.getJSONArray("aaData")
        } catch (e: Exception) {
            Log.e("FetchAlbumsTask", "${e.message}")
            throw e
        }
    }
}

fun OkHttpClient.prepareCall(offset: Int, length: Int = 100, url: String): Call {
    return newCall(
        Request.Builder().get().url(
            url.format(offset, length)
        )
            .build()
    )
}

fun Response.unwrap(): ResponseBody =
    if (isSuccessful) body!!
    else throw IOException("HTTP $code")

class FetchAlbumsJsonArray(
    private val networkClient: OkHttpClient,
    private val url: String = BASE_URL,
    val offset: Int,
    val length: Int = 100
) :
    Callable<List<AlbumInfo>> {
    override fun call(): List<AlbumInfo> {
        val json = networkClient.prepareCall(offset, length, url)
            .execute()
            .unwrap()
            .string()
        return try {
            val obj = JSONObject(json)
            mapToAlbumList(obj.getJSONArray("aaData"))
        } catch (e: Exception) {
            Log.e("FetchAlbumsJsonArray", "${e.message}")
            throw e
        }
    }
}

fun mapToAlbumList(
    jsonAlbumsArray: JSONArray,
    parser: HrefStringParser = HrefStringParser(
        RegexFactory().regex<String>(),
        RegexFactory().regex<Link>()
    )
): List<AlbumInfo> {
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
        throw e
    }
}
