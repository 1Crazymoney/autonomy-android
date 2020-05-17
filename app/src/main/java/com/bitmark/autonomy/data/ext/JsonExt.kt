/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.ext

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

inline fun <reified T> Gson.fromJson(json: String) =
    this.fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.toJson(value: T) =
    this.toJson(value, object : TypeToken<T>() {}.type)

fun newGsonInstance() = Gson().newBuilder().excludeFieldsWithoutExposeAnnotation().create()

fun JSONArray.toStringArrayList(): ArrayList<String> {
    val result = mutableListOf<String>()
    for (i in 0 until length()) {
        result.add(getJSONObject(i).toString())
    }
    return ArrayList(result)
}