/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote.api.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class RegisterAccountRequest(
    @SerializedName("enc_pub_key")
    @Expose
    val encPubKey: String,

    @SerializedName("metadata")
    @Expose
    val metadata: Map<String, String>
) : Request