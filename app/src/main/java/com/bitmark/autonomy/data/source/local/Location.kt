/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local


class Location private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: Location? = null

        fun getInstance(): Location {
            if (INSTANCE == null) {
                synchronized(Location::class) {
                    if (INSTANCE == null) {
                        INSTANCE = Location()
                    }
                }
            }

            return INSTANCE!!
        }
    }

    var lat: Double? = null

    var lng: Double? = null

    fun isAvailable() = lat != null && lng != null

    override fun toString(): String {
        return if (isAvailable()) "($lat, $lng)" else ""
    }
}

fun Location.apply(l: android.location.Location): Location {
    lat = l.latitude
    lng = l.longitude
    return this
}