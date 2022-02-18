package cc.woverflow.crashpatch.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser

private val parser = JsonParser()

fun String.asJsonObject(): JsonObject = parser.parse(this).asJsonObject