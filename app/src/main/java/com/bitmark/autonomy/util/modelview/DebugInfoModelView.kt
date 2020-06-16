/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.data.model.DebugInfoData
import com.bitmark.autonomy.data.model.Location
import com.bitmark.autonomy.data.model.NeighborProfile

data class DebugInfoModelView(
    val metric: NeighborProfile,

    val users: Int,

    val aqi: Int,

    val symptoms: Int,

    val location: Location,

    val areaId: String?
) : ModelView {

    companion object {
        fun newInstance(debugInfoData: DebugInfoData, location: Location, areaId: String?) =
            DebugInfoModelView(
                debugInfoData.metric,
                debugInfoData.users,
                debugInfoData.aqi,
                debugInfoData.symptoms,
                location,
                areaId
            )
    }
}