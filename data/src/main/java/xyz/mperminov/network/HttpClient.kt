package xyz.mperminov.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import xyz.mperminov.raw.BuildConfig
import java.util.concurrent.TimeUnit


object HttpClient {
    val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private const val CALL_TIMEOUT: Long = 10
    val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                if (BuildConfig.DEBUG)
                    logger
                chain.proceed(chain.request())
            }
            .addNetworkInterceptor { chain ->
                Thread.sleep(3000)
                chain.proceed(chain.request())
            }
            .callTimeout(CALL_TIMEOUT, TimeUnit.SECONDS)
        builder.build()
    }
}