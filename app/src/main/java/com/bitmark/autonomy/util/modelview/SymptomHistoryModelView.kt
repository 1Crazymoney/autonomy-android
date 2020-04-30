/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.data.model.Location
import com.bitmark.autonomy.data.model.SymptomData
import com.bitmark.autonomy.data.model.SymptomHistoryData


data class SymptomHistoryModelView(
    val symptoms: List<SymptomData>,

    val location: Location,

    val timestamp: Long
) : ModelView {
    companion object {
        fun newInstance(data: SymptomHistoryData) =
            SymptomHistoryModelView(data.symptoms, data.location, data.timestamp)
    }
}

fun SymptomHistoryModelView.joinSymptoms() =
    if (symptoms.isEmpty()) "" else symptoms.joinToString(", ") { s -> s.name.capitalize() }