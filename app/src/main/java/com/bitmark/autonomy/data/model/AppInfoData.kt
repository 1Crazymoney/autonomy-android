/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class AppInfoData(
    @Expose
    @SerializedName("android")
    val androidAppInfo: AndroidAppInfo,

    @Expose
    @SerializedName("ios")
    val iosAppInfo: IosAppInfo,

    @Expose
    @SerializedName("server")
    val serverInfo: ServerInfo,

    @Expose
    @SerializedName("system_version")
    val systemVersion: String,

    @Expose
    @SerializedName("docs")
    val docs: Docs
) : Data

data class Docs(
    @Expose
    @SerializedName("eula")
    val eula: String
)

data class AndroidAppInfo(
    @Expose
    @SerializedName("minimum_client_version")
    val requiredVersion: Int,

    @Expose
    @SerializedName("app_update_url")
    val updateUrl: String
)

data class IosAppInfo(
    @Expose
    @SerializedName("minimum_client_version")
    val requiredVersion: Int,

    @Expose
    @SerializedName("app_update_url")
    val updateUrl: String
)

data class ServerInfo(
    @Expose
    @SerializedName("version")
    val version: String,

    @Expose
    @SerializedName("bitmark_account_number")
    val bmAccountNumber: String,

    @Expose
    @SerializedName("enc_pub_key")
    val encPubKey: String
)