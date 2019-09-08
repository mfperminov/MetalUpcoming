package xyz.mperminov.mapper

import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import xyz.mperminov.model.Album
import java.util.concurrent.*


object MapperThreadPool {

    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
    private const val KEEP_ALIVE_TIME: Long = 1
    private val KEEP_ALIVE_TIME_UNIT: TimeUnit = TimeUnit.SECONDS
    private val taskQueue: BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>()
    private val executorService: ExecutorService = ThreadPoolExecutor(
        NUMBER_OF_CORES,
        NUMBER_OF_CORES,
        KEEP_ALIVE_TIME,
        KEEP_ALIVE_TIME_UNIT,
        taskQueue,
        BackgroundThreadFactory()
    )
    private val taskList: MutableList<Callable<Album>> = ArrayList()

    fun addCallable(callable: Callable<Album>) {
        taskList.add(callable)
    }

    fun getAll(): List<Album> {
        val albumFutures: List<Future<Album>> = executorService.invokeAll(taskList)
        val albumsMapped = albumFutures.map { it.get() }
        taskList.clear()
        return albumsMapped
    }

    private class BackgroundThreadFactory : ThreadFactory {

        private val TAG = this.javaClass.simpleName

        override fun newThread(runnable: Runnable): Thread {
            val thread = Thread(runnable)
            thread.name = "AlbumMappingThread${++sTag}"
            thread.priority = THREAD_PRIORITY_BACKGROUND

            // A exception handler is created to log the exception from threads
            thread.uncaughtExceptionHandler =
                Thread.UncaughtExceptionHandler { t, ex ->
                    Log.e(
                        TAG,
                        t.name + " encountered an error: " + ex.message
                    )
                }
            return thread
        }

        companion object {
            private var sTag = 1
        }
    }
}