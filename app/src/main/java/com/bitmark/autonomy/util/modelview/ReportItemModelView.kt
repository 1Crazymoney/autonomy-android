/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.data.model.ReportItemData


data class ReportItemModelView(
    val name: String,

    val value: Float?,

    val changeRate: Float?,

    val distribution: Map<String, Int>,

    val type: String,

    val startedAt: String
) : ModelView {
    companion object {
        fun newInstance(reportItemData: ReportItemData, type: String, startedAt: String) =
            ReportItemModelView(
                reportItemData.name,
                reportItemData.value,
                reportItemData.changeRate,
                reportItemData.distribution,
                type,
                startedAt
            )
    }
}

fun ReportItemModelView.isNotSupported() = value == null || changeRate == null