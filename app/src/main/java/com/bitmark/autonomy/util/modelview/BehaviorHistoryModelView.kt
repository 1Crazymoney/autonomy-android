/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.data.model.BehaviorData
import com.bitmark.autonomy.data.model.BehaviorHistoryData
import com.bitmark.autonomy.data.model.Location


class BehaviorHistoryModelView(
    val behaviors: List<BehaviorData>,

    val location: Location,

    val timestamp: Long
) : ModelView {
    companion object {
        fun newInstance(data: BehaviorHistoryData) =
            BehaviorHistoryModelView(data.behaviors, data.location, data.timestamp)
    }
}

fun BehaviorHistoryModelView.joinSymptoms() =
    if (behaviors.isEmpty()) "" else behaviors.joinToString(", ") { b -> b.name.capitalize() }