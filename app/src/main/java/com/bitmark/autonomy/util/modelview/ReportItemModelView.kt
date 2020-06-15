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

    val type: String,

    val startedAt: Long
) : ModelView {
    companion object {
        fun newInstance(reportItemData: ReportItemData, type: String, startedAt: Long) =
            ReportItemModelView(
                reportItemData.name,
                reportItemData.value,
                reportItemData.changeRate,
                type,
                startedAt
            )
    }
}

fun ReportItemModelView.isNotSupported() = value == null || changeRate == null