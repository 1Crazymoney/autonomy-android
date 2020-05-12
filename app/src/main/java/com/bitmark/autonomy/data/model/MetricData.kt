/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SymptomMetricData2(
    @Expose
    @SerializedName("me")
    val mine: Metric1,

    @Expose
    @SerializedName("community")
    val community: Metric2
) : Data

data class BehaviorMetricData2(
    @Expose
    @SerializedName("me")
    val mine: Metric1,

    @Expose
    @SerializedName("community")
    val community: Metric2
)

data class Metric1(

    @Expose
    @SerializedName("total_today")
    val totalToday: Int,

    @Expose
    @SerializedName("delta")
    val delta: Float
) : Data

data class Metric2(
    @Expose
    @SerializedName("avg_today")
    val avgToday: Float,

    @Expose
    @SerializedName("delta")
    val delta: Float
) : Data