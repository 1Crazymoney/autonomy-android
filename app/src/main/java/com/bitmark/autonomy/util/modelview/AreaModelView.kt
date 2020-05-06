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

fun toDrawableRes(value: Int) = when {
    value == 0 -> R.drawable.ic_circle_mine_shaft_2
    value < 34 -> R.drawable.ic_circle_red
    value < 67 -> R.drawable.ic_circle_yellow
    else -> R.drawable.ic_circle_green
}

fun toColorRes(value: Int) = when {
    value == 0 -> R.color.mine_shaft_2
    value < 34 -> R.color.persian_red
    value < 67 -> R.color.gold_tip
    else -> R.color.apple
}

fun toOpacityColorRes(value: Int) = when {
    value == 0 -> R.color.mine_shaft_2_40
    value < 34 -> R.color.persian_red_40
    value < 67 -> R.color.gold_tip_40
    else -> R.color.apple_40
}