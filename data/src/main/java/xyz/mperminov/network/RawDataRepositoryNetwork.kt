package xyz.mperminov.network

import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import okhttp3.Request
import xyz.mperminov.RawDataRepository

class RawDataRepositoryNetwork(private val handler: Handler) : RawDataRepository {
    private lateinit var jsonGettingTask: JsonGettingTask

    override fun getRawData() {
        jsonGettingTask = JsonGettingTask(handler)
        val rawJsonString = jsonGettingTask.execute().get()
        if (rawJsonString != "") {
            handler.sendMessage(
                Message.obtain(handler, PROCEED_DATA, rawJsonString)
            )
        }
    }

    override fun cancel() {
        jsonGettingTask.cancel(true)
    }

    companion object {
        const val ERROR = 42
        const val END_PROGRESS = 40
        const val BEGIN_PROGRESS = 41
        const val PROCEED_DATA = 43
    }
}

internal class JsonGettingTask(private val handler: Handler) :
    AsyncTask<Unit, Unit, String>() {

    override fun onPreExecute() {
        super.onPreExecute()
        handler.sendEmptyMessage(RawDataRepositoryNetwork.BEGIN_PROGRESS)
    }

    override fun doInBackground(vararg p0: Unit?): String {
        val httpClient = HttpClient.client
        val call = httpClient.newCall(
            Request.Builder().url(RawDataRepository.BASE_URL)
                .build()
        )
        try {
            val response = call.execute()
            if (response.isSuccessful && response.body != null) {
                val rawJson = response.body?.string()
                response.body!!.close()
                if (rawJson != null)
                    return rawJson

            }
        } catch (e: Exception) {
            handler.sendMessage(Message.obtain(handler, RawDataRepositoryNetwork.ERROR, e))
        }
        return ""
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        handler.sendEmptyMessage(RawDataRepositoryNetwork.END_PROGRESS)
    }

    override fun onCancelled(result: String?) {
        super.onCancelled(result)
        if (result != null && result != "") {
            handler.sendEmptyMessage(RawDataRepositoryNetwork.END_PROGRESS)
            handler.sendMessage(
                Message.obtain(handler, RawDataRepositoryNetwork.PROCEED_DATA, result)
            )
        } else {
            handler.sendEmptyMessage(RawDataRepositoryNetwork.END_PROGRESS)
        }
    }
}