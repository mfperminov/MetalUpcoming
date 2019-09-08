package xyz.mperminov.network

import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import okhttp3.Request
import xyz.mperminov.RawDataRepository

class RawDataRepositoryNetwork(private val handler: Handler) : RawDataRepository {
    private val jsonGettingTask = JsonGettingTask(handler)

    override fun getRawData() {
        val rawJsonString = jsonGettingTask.execute().get()
        if (rawJsonString != "") {
            handler.sendMessage(
                Message.obtain(handler, 43, rawJsonString)
            )
        } else {
            handler.sendMessage(
                Message.obtain(handler, 42, Throwable("Unknown error"))
            )
        }
    }

    override fun cancel() {
        jsonGettingTask.cancel(true)
    }
}

internal class JsonGettingTask(private val handler: Handler) :
    AsyncTask<Unit, Unit, String>() {

    override fun onPreExecute() {
        super.onPreExecute()
        handler.sendEmptyMessage(40)
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
            handler.sendMessage(Message.obtain(handler, 42, e))
        }
        return ""
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        handler.sendEmptyMessage(41)
    }

    override fun onCancelled(result: String?) {
        super.onCancelled(result)
        handler.sendEmptyMessage(41)
    }
}