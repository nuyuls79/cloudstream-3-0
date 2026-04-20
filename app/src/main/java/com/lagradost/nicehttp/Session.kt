package com.lagradost.nicehttp

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class Session(client: OkHttpClient) : Requests() {

    init {
        val jar = CustomCookieJar()

        val newClient = client.newBuilder()
            .cookieJar(jar)
            .build()

        baseClient = newClient
    }

    class CustomCookieJar : CookieJar {

        var cookies: Map<String, Cookie> = emptyMap()

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookies.values.toList()
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val newCookies = cookies.associateBy { it.name }
            this.cookies = this.cookies + newCookies
        }
    }
}