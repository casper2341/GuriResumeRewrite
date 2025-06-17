package com.guri.guriresumerewrite

import com.google.gson.Gson

fun <T> Gson.jsonToObjectOrNull(json: String?, clazz: Class<T>): T? =
    try {
        fromJson(json, clazz)
    } catch (ignored: Exception) {
        null
    }
