package xyz.mperminov.metalupcoming

import android.app.Application
import android.os.Bundle
import net.aquadc.properties.persistence.memento.PersistableProperties
import okhttp3.OkHttpClient
import java.io.Closeable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class App : Application() {

    private val okHttp = lazy {
        OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val io =
        ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            30, TimeUnit.SECONDS, LinkedBlockingQueue()
        )

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : LifecycleCallbacksInjector() {
            override fun <VM> injectInto(
                activity: InjectableActivity<VM>,
                savedInstanceState: Bundle?
            )
                where VM : PersistableProperties, VM : Closeable {
                when (activity) {
                    is MainActivity -> activity.vm = AlbumsViewModel(
                        okHttp,
                        io,
                        savedInstanceState?.getParcelable("vm")
                    )
                    else -> throw AssertionError()
                }
            }
        })
    }
}