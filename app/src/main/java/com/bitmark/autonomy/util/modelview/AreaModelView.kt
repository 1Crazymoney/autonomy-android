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

fun toDrawableRes(value: Float) = when {
    value == 0f -> R.drawable.ic_circle_mine_shaft_2
    value < 34f -> R.drawable.ic_circle_red
    value < 67f -> R.drawable.ic_circle_yellow
    else -> R.drawable.ic_circle_green
}

fun toColorRes(value: Float) = when {
    value == 0f -> R.color.mine_shaft_2
    value < 34f -> R.color.persian_red
    value < 67f -> R.color.gold_tip
    else -> R.color.apple
}