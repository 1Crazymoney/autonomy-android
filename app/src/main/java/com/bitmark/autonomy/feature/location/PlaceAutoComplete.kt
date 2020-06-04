/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.location


data class PlaceAutoComplete(
    val id: String,
    val primaryText: String,
    val secondaryText: String,
    val desc: String,
    var score: Float? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceAutoComplete

        if (id != other.id) return false
        if (primaryText != other.primaryText) return false
        if (secondaryText != other.secondaryText) return false
        if (desc != other.desc) return false
        if (score != other.score) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + primaryText.hashCode()
        result = 31 * result + secondaryText.hashCode()
        result = 31 * result + desc.hashCode()
        result = 31 * result + (score?.hashCode() ?: 0)
        return result
    }
}