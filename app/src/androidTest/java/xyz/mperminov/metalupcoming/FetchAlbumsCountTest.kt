package xyz.mperminov.metalupcoming

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FetchAlbumsCountTest {
    lateinit var mockWebServer: MockWebServer
    private val okHttpClient = OkHttpClient()
    private val validJson = """
        { 
	"iTotalRecords": 333,
	"iTotalDisplayRecords": 333,
	"sEcho": 0,
	"aaData": [
			[
			"<a href=\"https://www.metal-archives.com/bands/Decayed_Flesh/3540449071\">Decayed Flesh</a>", 
			"<a href=\"https://www.metal-archives.com/albums/Decayed_Flesh/Eternal_Misery/839844\">Eternal Misery</a>",
			"Full-length", 
			"Brutal Death Metal", 
			
			"June 13th, 2020"
		]
        ]
    }
    """.trimIndent()

    private val invalidJson = """
        { 
	"iTotalRecords": rm,
	"iTotalDisplayRecords": rf,
	"sEcho": 0,
	"aaData": [
			[
			"<a href=\"https://www.metal-archives.com/bands/Decayed_Flesh/3540449071\">Decayed Flesh</a>", 
			"<a href=\"https://www.metal-archives.com/albums/Decayed_Flesh/Eternal_Misery/839844\">Eternal Misery</a>",
			"Full-length", 
			"Brutal Death Metal", 
			
			"June 13th, 2020"
		]
        ]
    }
    """.trimIndent()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @Test
    fun successGettingTotalCountOfAlbums() {
        mockWebServer.enqueue(MockResponse().setBody(validJson))
        val totalCount =
            FetchAlbumsCount(okHttpClient, url = mockWebServer.url("/").toString()).call()
        assertEquals(333, totalCount)
    }

    @Test
    fun failGettingTotalCountOfAlbums() {
        mockWebServer.enqueue(MockResponse().setBody(invalidJson))
        val totalCount =
            FetchAlbumsCount(okHttpClient, url = mockWebServer.url("/").toString()).call()
        assertEquals(0, totalCount)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
