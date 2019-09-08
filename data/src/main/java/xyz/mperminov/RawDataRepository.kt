package xyz.mperminov

interface RawDataRepository {

    companion object {
        const val BASE_URL = "https://www.metal-archives.com/release/ajax-upcoming/json/1"
    }

    fun getRawData()

    fun cancel()
}