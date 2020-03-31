/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote.api.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class RequestHelpRequest(
    @Expose
    @SerializedName("subject")
    val subject: String,

    @Expose
    @SerializedName("exact_needs")
    val exactNeed: String,

    @Expose
    @SerializedName("meeting_location")
    val meetingLocation: String,

    @Expose
    @SerializedName("contact_info")
    val contactInfo: String
) : Request