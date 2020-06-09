/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.data.model.ResourceRatingData


data class ResourceRatingModelView(val id: String, val name: String, var score: Int) : ModelView {
    companion object {
        fun newInstance(rating: ResourceRatingData) =
            ResourceRatingModelView(rating.id, rating.name, rating.score)
    }
}

fun ResourceRatingModelView.toResData() = ResourceRatingData(id, name, score)