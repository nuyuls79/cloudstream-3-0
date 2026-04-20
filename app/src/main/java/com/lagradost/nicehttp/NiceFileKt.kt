package com.lagradost.nicehttp

fun Map<String, String>.toNiceFiles(): List<NiceFile> {

    val list = ArrayList<NiceFile>(this.size)

    for ((key, value) in this) {
        list.add(
            NiceFile(
                name = key,
                fileName = value
            )
        )
    }

    return list
}