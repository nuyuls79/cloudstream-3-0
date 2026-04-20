package com.lagradost.nicehttp

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NiceHttp {

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
