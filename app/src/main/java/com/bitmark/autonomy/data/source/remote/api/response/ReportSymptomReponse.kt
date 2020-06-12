/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote.api.response

import com.bitmark.autonomy.data.model.InstitutionData
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class ReportSymptomReponse(

    @Expose
    @SerializedName("guide")
    val institutions: List<InstitutionData>,

    @Expose
    @SerializedName("official")
    val official: Int
): Response