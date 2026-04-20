package com.lagradost.nicehttp

import okhttp3.Headers

data class NiceResponse(
    val code: Int,
    val text: String?,
    val headers: Headers?,
    val url: String
) {
    val isSuccessful: Boolean
        get() = code in 200..299
}
