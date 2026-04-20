package com.lagradost.nicehttp

import okhttp3.Headers
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.StringWriter
import kotlin.reflect.KClass

class NiceResponse(
    private val okhttpResponse: Response,
    private val parser: ResponseParser? = null
) {

    companion object {
        const val MAX_TEXT_SIZE: Long = 5_000_000
    }

    private var consumedBody = false

    val code: Int = okhttpResponse.code
    val headers: Headers = okhttpResponse.headers
    val isSuccessful: Boolean = okhttpResponse.isSuccessful

    val body: ResponseBody by lazy {
        okhttpResponse.body!!
    }

    val url: String by lazy {
        okhttpResponse.request.url.toString()
    }

    val cookies: Map<String, String> by lazy {
        getCookies(okhttpResponse.headers, "set-cookie")
    }

    val size: Long? by lazy {
        okhttpResponse.header("content-length")?.toLongOrNull()
    }

    val text: String by lazy {

        synchronized(this) {

            if (consumedBody) {
                return@synchronized textLarge
            }

            consumedBody = true

            val reader = body.charStream()

            val length = size
            if (length != null && length > MAX_TEXT_SIZE) {
                throw IllegalStateException(
                    "Called .text on a text file with Content-Length > $MAX_TEXT_SIZE"
                )
            }

            val writer = StringWriter()
            val buffer = CharArray(8192)

            var total = 0L
            var read = reader.read(buffer)

            while (read >= 0 && total < MAX_TEXT_SIZE) {
                writer.write(buffer, 0, read)
                total += read
                read = reader.read(buffer)
            }

            if (total >= MAX_TEXT_SIZE) {
                throw IllegalStateException(
                    "Called .text on a text file above $MAX_TEXT_SIZE"
                )
            }

            reader.close()
            body.close()

            writer.toString()
        }
    }

    val textLarge: String by lazy {

        synchronized(this) {

            if (consumedBody) {
                println("Warning: Using textLarge after body consumed, fallback to text")
                return@synchronized text
            }

            consumedBody = true
            val result = body.string()
            body.close()
            result
        }
    }

    val document: Document by lazy {
        Jsoup.parse(text)
    }

    val documentLarge: Document by lazy {
        Jsoup.parse(textLarge)
    }

    inline fun <reified T : Any> parsed(): T {
        val p = parser ?: error("ResponseParser not set")
        return p.parse(text, T::class)
    }

    inline fun <reified T : Any> parsedLarge(): T {
        val p = parser ?: error("ResponseParser not set")
        return p.parse(textLarge, T::class)
    }

    inline fun <reified T : Any> parsedSafe(): T? {
        return try {
            val p = parser ?: return null
            p.parseSafe(text, T::class)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    inline fun <reified T : Any> parsedSafeLarge(): T? {
        return try {
            val p = parser ?: return null
            p.parseSafe(textLarge, T::class)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun toString(): String {
        return text
    }
}