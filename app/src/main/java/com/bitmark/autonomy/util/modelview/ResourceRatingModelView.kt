/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

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