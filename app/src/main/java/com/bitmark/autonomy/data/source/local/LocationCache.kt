/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local

import android.location.Location


class LocationCache private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: LocationCache? = null

        fun getInstance(): LocationCache {
            if (INSTANCE == null) {
                synchronized(LocationCache::class) {
                    if (INSTANCE == null) {
                        INSTANCE = LocationCache()
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

fun LocationCache.apply(l: Location) {
    lat = l.latitude
    lng = l.longitude
}