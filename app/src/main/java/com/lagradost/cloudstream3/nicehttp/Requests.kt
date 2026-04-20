package com.lagradost.nicehttp

import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody

object Requests {

    fun get(url: String, headers: Map<String, String> = emptyMap()): NiceResponse {
        val builder = Request.Builder().url(url)

        headers.forEach {
            builder.addHeader(it.key, it.value)
        }

        val request = builder.get().build()

        val response = NiceHttp.client.newCall(request).execute()

        return ResponseParser.parse(response)
    }

    fun post(
        url: String,
        data: Map<String, String>,
        headers: Map<String, String> = emptyMap()
    ): NiceResponse {

        val formBuilder = FormBody.Builder()

        data.forEach {
            formBuilder.add(it.key, it.value)
        }

        val body: RequestBody = formBuilder.build()

        val builder = Request.Builder()
            .url(url)
            .post(body)

        headers.forEach {
            builder.addHeader(it.key, it.value)
        }

        val request = builder.build()

        val response = NiceHttp.client.newCall(request).execute()

        return ResponseParser.parse(response)
    }
}
