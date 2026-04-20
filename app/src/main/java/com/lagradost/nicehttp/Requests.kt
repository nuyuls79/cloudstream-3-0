package com.lagradost.nicehttp

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open class Requests(
    var baseClient: OkHttpClient = OkHttpClient(),
    var defaultHeaders: Map<String, String> = mapOf("user-agent" to "NiceHttp"),
    var defaultReferer: String? = null,
    var defaultData: Map<String, String> = emptyMap(),
    var defaultCookies: Map<String, String> = emptyMap(),
    var defaultCacheTime: Int = 0,
    var defaultCacheTimeUnit: TimeUnit = TimeUnit.MINUTES,
    var defaultTimeOut: Long = 0L,
    var responseParser: ResponseParser? = null
) {

    companion object {

        suspend fun Call.await(): Response =
            suspendCancellableCoroutine { cont ->

                val callback = ContinuationCallback(this, cont)

                enqueue(callback)

                cont.invokeOnCancellation {
                    try {
                        cancel()
                    } catch (_: Throwable) {}
                }
            }
    }

    suspend fun custom(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        referer: String? = null,
        params: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
        data: Map<String, String>? = defaultData,
        files: List<NiceFile>? = null,
        json: Any? = null,
        requestBody: RequestBody? = null,
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true,
        parser: ResponseParser? = responseParser
    ): NiceResponse {

        val request = requestCreator(
            method = method,
            url = url,
            headers = headers + defaultHeaders,
            referer = referer ?: defaultReferer,
            params = params,
            cookies = cookies + defaultCookies,
            data = data,
            files = files,
            json = json,
            requestBody = requestBody,
            cacheTime = cacheTime,
            cacheUnit = cacheUnit,
            responseParser = parser
        )

        var client = baseClient

        if (timeout > 0) {
            client = client.newBuilder()
                .callTimeout(timeout, TimeUnit.MILLISECONDS)
                .build()
        }

        if (interceptor != null) {
            client = client.newBuilder()
                .addInterceptor(interceptor)
                .build()
        }

        val response = client.newCall(request).await()

        return parser?.parse(response) ?: NiceResponse(response)
    }

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        referer: String? = null,
        params: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true,
        parser: ResponseParser? = responseParser
    ) = custom(
        "GET",
        url,
        headers,
        referer,
        params,
        cookies,
        null,
        null,
        null,
        null,
        allowRedirects,
        cacheTime,
        cacheUnit,
        timeout,
        interceptor,
        verify,
        parser
    )

    suspend fun post(
        url: String,
        headers: Map<String, String> = emptyMap(),
        referer: String? = null,
        params: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
        data: Map<String, String>? = defaultData,
        files: List<NiceFile>? = null,
        json: Any? = null,
        requestBody: RequestBody? = null,
        allowRedirects: Boolean = true,
        cacheTime: Int = defaultCacheTime,
        cacheUnit: TimeUnit = defaultCacheTimeUnit,
        timeout: Long = defaultTimeOut,
        interceptor: Interceptor? = null,
        verify: Boolean = true,
        parser: ResponseParser? = responseParser
    ) = custom(
        "POST",
        url,
        headers,
        referer,
        params,
        cookies,
        data,
        files,
        json,
        requestBody,
        allowRedirects,
        cacheTime,
        cacheUnit,
        timeout,
        interceptor,
        verify,
        parser
    )

    suspend fun delete(
        url: String,
        headers: Map<String, String> = emptyMap(),
        referer: String? = null,
        params: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
        data: Map<String, String>? = defaultData
    ) = custom("DELETE", url, headers, referer, params, cookies, data)

    suspend fun put(
        url: String,
        headers: Map<String, String> = emptyMap(),
        referer: String? = null,
        params: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
        data: Map<String, String>? = defaultData
    ) = custom("PUT", url, headers, referer, params, cookies, data)

    suspend fun patch(
        url: String,
        headers: Map<String, String> = emptyMap(),
        referer: String? = null,
        params: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
        data: Map<String, String>? = defaultData
    ) = custom("PATCH", url, headers, referer, params, cookies, data)

    suspend fun head(
        url: String,
        headers: Map<String, String> = emptyMap(),
        referer: String? = null
    ) = custom("HEAD", url, headers, referer)

    suspend fun options(
        url: String,
        headers: Map<String, String> = emptyMap()
    ) = custom("OPTIONS", url, headers)
}