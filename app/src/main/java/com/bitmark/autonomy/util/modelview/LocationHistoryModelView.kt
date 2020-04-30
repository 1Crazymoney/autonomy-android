/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.data.model.Location
import com.bitmark.autonomy.data.model.LocationHistoryData


class LocationHistoryModelView(
    val location: Location,

    val timestamp: Long
) : ModelView {
    companion object {
        fun newInstance(data: LocationHistoryData) =
            LocationHistoryModelView(data.location, data.timestamp)
    }
}