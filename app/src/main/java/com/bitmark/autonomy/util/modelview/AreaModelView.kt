/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import android.os.Parcelable
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.AreaData
import com.bitmark.autonomy.data.model.Location
import kotlinx.android.parcel.Parcelize
import kotlin.math.abs
import kotlin.math.roundToInt

@Parcelize
data class AreaModelView(
    val id: String,

    var alias: String,

    val location: Location,

    val score: Float?
) : ModelView, Parcelable {
    companion object {
        fun newInstance(area: AreaData) =
            AreaModelView(area.id, area.alias, area.location, area.score)
    }
}

fun toColorRes(value: Int) = when {
    value == 0 -> R.color.concord
    value < 34 -> R.color.persian_red
    value < 67 -> R.color.gold_tip
    else -> R.color.apple
}

fun toOpacityColorRes(value: Int) = when {
    value == 0 -> R.color.concord_40
    value < 34 -> R.color.persian_red_40
    value < 67 -> R.color.gold_tip_40
    else -> R.color.apple_40
}

fun formatDelta(delta: Float): String {
    val absDelta = abs(delta)
    return when {
        absDelta >= 1000f -> "%d%%".format(absDelta.roundToInt())
        absDelta >= 100f -> "%.01f%%".format(absDelta)
        else -> "%.02f%%".format(absDelta)
    }
}
