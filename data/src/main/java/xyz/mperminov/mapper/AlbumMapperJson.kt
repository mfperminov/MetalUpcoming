package xyz.mperminov.mapper

import org.json.JSONArray
import org.json.JSONObject
import xyz.mperminov.model.Album
import xyz.mperminov.parser.HrefStringParser
import xyz.mperminov.parser.PARSE_ERROR
import java.util.concurrent.Callable

class AlbumMapperJson(private val stringParser: HrefStringParser) : AlbumMapper {

    private fun getJsonObject(rawData: String) = JSONObject(rawData.replace("\"sEcho\": ,", ""))
    override fun parse(rawData: String): List<Album> {
        val jsonObject = getJsonObject(rawData)
        val aaDataArray = jsonObject.getJSONArray("aaData")

        for (i in 0 until aaDataArray.length()) {
            val task = parseJsonAlbumArray(aaDataArray.getJSONArray(i))
            MapperThreadPool.addCallable(task)
        }

        val results = MapperThreadPool.getAll()

        return results.filter { it != Album.NONE }.distinct()
    }

    private fun parseJsonAlbumArray(jsonArray: JSONArray?): Callable<Album> {
        return ParseAlbumTask(jsonArray, stringParser)
    }
}

class ParseAlbumTask(
    private val jsonArray: JSONArray?,
    private val stringParser: HrefStringParser
) :
    Callable<Album> {
    override fun call(): Album {
        if (jsonArray == null) return Album.NONE
        val bandName = stringParser.getTextInsideHrefTag(jsonArray.getString(0))
        val bandLink = stringParser.getLinkFromHrefTag(jsonArray.getString(0))
        val albumName = stringParser.getTextInsideHrefTag(jsonArray.getString(1))
        val albumLink = stringParser.getLinkFromHrefTag(jsonArray.getString(1))
        val type: Album.TYPE?
        try {
            type = Album.TYPE.fromString(jsonArray.getString(2))
        } catch (e: Exception) {
            return Album.NONE
        }
        val genre = jsonArray.getString(3)
        val date = jsonArray.getString(4)
        return if (bandName != PARSE_ERROR && bandLink != PARSE_ERROR && albumLink != PARSE_ERROR && albumName != PARSE_ERROR)
            Album(bandName, bandLink, albumName, albumLink, type, genre, date)
        else Album.NONE
    }
}
