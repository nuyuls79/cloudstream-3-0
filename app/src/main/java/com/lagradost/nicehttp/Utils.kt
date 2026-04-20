package com.lagradost.nicehttp

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

private val mustHaveBody = listOf("POST", "PUT")
private val cantHaveBody = listOf("GET", "HEAD")

fun addParamsToUrl(url: String, params: Map<String, String>): String {
    var result = url
    for ((key, value) in params) {
        result = appendUri(result, "$key=$value")
    }
    return result
}

fun appendUri(uri: String, appendQuery: String): String {
    val parsed = URI(uri)

    val query = if (parsed.query != null) {
        parsed.query + "&" + appendQuery
    } else {
        appendQuery
    }

    return URI(
        parsed.scheme,
        parsed.authority,
        parsed.path,
        query,
        parsed.fragment
    ).toString()
}

fun getCache(cacheTime: Int, cacheUnit: TimeUnit): CacheControl {
    return CacheControl.Builder()
        .maxAge(cacheTime, cacheUnit)
        .build()
}

fun getCookies(headers: Headers, cookieKey: String): Map<String, String> {

    val cookies = mutableMapOf<String, String>()

    headers.forEach { (name, value) ->
        if (name.equals(cookieKey, true)) {

            val cookie = value.substringBefore(";")

            val parts = cookie.split("=")

            val key = parts.getOrNull(0)?.trim().orEmpty()
            val v = parts.getOrNull(1)?.trim().orEmpty()

            if (key.isNotBlank() && v.isNotBlank()) {
                cookies[key] = v
            }
        }
    }

    return cookies
}

fun getHeaders(
    headers: Map<String, String>,
    referer: String?,
    cookie: Map<String, String>
): Headers {

    val finalHeaders = headers.toMutableMap()

    if (referer != null) {
        finalHeaders["referer"] = referer
    }

    if (cookie.isNotEmpty()) {
        finalHeaders["Cookie"] =
            cookie.entries.joinToString(";") { "${it.key}=${it.value}" }
    }

    return Headers.of(finalHeaders)
}

fun getData(
    method: String,
    data: Map<String, String>?,
    files: List<NiceFile>?,
    json: Any?,
    requestBody: RequestBody?,
    parser: ResponseParser?
): RequestBody? {

    val upper = method.uppercase(Locale.ROOT)

    if (cantHaveBody.contains(upper)) return null

    if (requestBody != null) return requestBody

    if (!data.isNullOrEmpty()) {
        val builder = FormBody.Builder()

        data.forEach { (k, v) ->
            builder.addEncoded(k, v)
        }

        return builder.build()
    }

    if (json != null) {

        val jsonString = when (json) {
            is JSONObject -> json.toString()
            is JSONArray -> json.toString()
            is String -> json
            is JsonAsString -> json.string
            else -> parser?.writeValueAsString(json) ?: json.toString()
        }

        return RequestBody.create(
            MediaType.parse(RequestBodyTypes.JSON),
            jsonString
        )
    }

    if (!files.isNullOrEmpty()) {

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        for (file in files) {

            if (file.file != null) {

                val body = RequestBody.create(
                    file.fileType?.let { MediaType.parse(it) },
                    file.file
                )

                builder.addFormDataPart(
                    file.name,
                    file.fileName,
                    body
                )

            } else {

                builder.addFormDataPart(
                    file.name,
                    file.fileName
                )
            }
        }

        return builder.build()
    }

    if (mustHaveBody.contains(upper)) {
        return FormBody.Builder().build()
    }

    return null
}

fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {

    val trustAll = object : X509TrustManager {
        override fun checkClientTrusted(
            chain: Array<X509Certificate>,
            authType: String
        ) {}

        override fun checkServerTrusted(
            chain: Array<X509Certificate>,
            authType: String
        ) {}

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    val sslContext = SSLContext.getInstance("SSL")

    sslContext.init(null, arrayOf<TrustManager>(trustAll), SecureRandom())

    val factory = sslContext.socketFactory

    sslSocketFactory(factory, trustAll)

    hostnameVerifier { _, _ -> true }

    return this
}