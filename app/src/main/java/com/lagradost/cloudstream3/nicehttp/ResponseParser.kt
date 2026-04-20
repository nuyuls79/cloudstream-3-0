package com.lagradost.nicehttp

import okhttp3.Response

object ResponseParser {

    fun parse(response: Response): NiceResponse {
        val bodyString = response.body?.string()

        return NiceResponse(
            code = response.code,
            text = bodyString,
            headers = response.headers,
            url = response.request.url.toString()
        )
    }
}
