/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BehaviorHistoryData(
    @Expose
    @SerializedName("behaviors")
    val behaviors: List<BehaviorData>,

    @Expose
    @SerializedName("location")
    val location: Location,

    @Expose
    @SerializedName("timestamp")
    val timestamp: Long
) : Data