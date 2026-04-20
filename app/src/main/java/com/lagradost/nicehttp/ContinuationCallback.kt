package com.lagradost.nicehttp

import kotlinx.coroutines.CancellableContinuation
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.io.InterruptedIOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ContinuationCallback(
    private val call: Call,
    private val continuation: CancellableContinuation<Response>
) : Callback, (Throwable?) -> Unit {

    override fun invoke(cause: Throwable?) {
        try {
            call.cancel()
        } catch (_: Throwable) {
        }
    }

    override fun onFailure(call: Call, e: IOException) {
        println("Exception in NiceHttp: ${e.javaClass.name} ${e.message}")

        if (!call.isCanceled()) {
            continuation.resumeWithException(e)
        } else if (e is InterruptedIOException) {
            continuation.cancel(e)
        } else {
            e.printStackTrace()
        }
    }

    override fun onResponse(call: Call, response: Response) {
        continuation.resume(response)
    }
}