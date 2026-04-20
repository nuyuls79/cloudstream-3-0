package com.lagradost.nicehttp

import java.io.File

data class NiceFile(

    val name: String,

    val fileName: String,

    val file: File? = null,

    val fileType: String? = null
)