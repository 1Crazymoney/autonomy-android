/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.data.model.Metric1
import com.bitmark.autonomy.data.model.Metric2
import com.bitmark.autonomy.data.model.SymptomMetricData2


class SymptomMetricModelView(
    val mine: Metric1,

    val community: Metric2
) : ModelView {
    companion object {
        fun newInstance(metricData: SymptomMetricData2) =
            SymptomMetricModelView(metricData.mine, metricData.community)
    }
}