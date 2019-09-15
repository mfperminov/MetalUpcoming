package xyz.mperminov.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


object HttpClient {
    private const val CALL_TIMEOUT: Long = 10
    val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                Thread.sleep(3000)
                chain.proceed(chain.request())
            }
            .callTimeout(CALL_TIMEOUT, TimeUnit.SECONDS)
        builder.build()
    }
}