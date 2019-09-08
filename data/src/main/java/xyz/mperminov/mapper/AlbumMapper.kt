package xyz.mperminov.mapper

import xyz.mperminov.model.Album

interface AlbumMapper {

    fun parse(rawData: String): List<Album>
}