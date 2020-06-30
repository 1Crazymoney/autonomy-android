/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.ResourceData
import com.bitmark.autonomy.data.model.ResourceRatingData


data class ResourceRatingModelView(val resource: ResourceData, var score: Float, var ratings: Int) :
    ModelView {
    companion object {
        fun newInstance(rating: ResourceRatingData) =
            ResourceRatingModelView(rating.resource, rating.score, rating.ratings)

        fun newInstance(resource: ResourceModelView) =
            ResourceRatingModelView(resource.toResourceData(), 0f, 0)
    }
}

fun ResourceRatingModelView.toResourceRatingData() = ResourceRatingData(resource, score, ratings)

fun Int.ratingToDrawableRes() = when {
    this < 3 -> R.drawable.bg_circle_red
    this < 4 -> R.drawable.bg_circle_yellow
    else -> R.drawable.bg_circle_green
}

fun Float.ratingScoreToColorRes() = when {
    this == 0f -> R.color.white
    this <= 1.6f -> R.color.persian_red
    this <= 3.3f -> R.color.gold_tip
    else -> R.color.apple
}

fun Float.ratingScoreToDrawableRes() = when {
    this == 0f -> R.drawable.bg_circle_black
    this <= 1.6f -> R.drawable.bg_circle_red
    this <= 3.3f -> R.drawable.bg_circle_yellow
    else -> R.drawable.bg_circle_green
}

fun Float.ratingScoreToStatefulColorRes() = when {
    this == 0f -> R.color.color_white_stateful
    this <= 1.6f -> R.color.color_persian_red_stateful
    this <= 3.3f -> R.color.color_gold_tip_stateful
    else -> R.color.color_apple_stateful
}