/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class AccountData(
    @Expose
    @SerializedName("account_number")
    val accountNumber: String,

    @Expose
    @SerializedName("enc_pub_key")
    val encPubKey: String,

    @Expose
    @SerializedName("created_at")
    val createdAt: String,

    @Expose
    @SerializedName("updated_at")
    val updatedAt: String,

    @Expose
    @SerializedName("profile")
    val profile: Profile?,

    @Expose
    @SerializedName("auth_required")
    var authRequired: Boolean = false,

    @Expose
    @SerializedName("key_alias")
    var keyAlias: String
) : Data {
    companion object
}

data class Profile(
    @Expose
    @SerializedName("id")
    val id: String,

    @Expose
    @SerializedName("account_number")
    val accountNumber: String,

    @Expose
    @SerializedName("state")
    val state: State,

    @Expose
    @SerializedName("metadata")
    val metadata: Map<String, String>,

    @Expose
    @SerializedName("created_at")
    val createdAt: String,

    @Expose
    @SerializedName("updated_at")
    val updatedAt: String
) : Data

data class State(
    @Expose
    @SerializedName("last_active_time")
    val lastActiveTime: String,

    @Expose
    @SerializedName("location")
    val location: Location
) : Data

data class Location(
    @Expose
    @SerializedName("lat")
    val lat: Double,

    @Expose
    @SerializedName("lng")
    val lng: Double
) : Data

fun AccountData.Companion.newInstance(): AccountData = AccountData("", "", "", "", null, false, "")

fun AccountData.isRegistered(): Boolean = accountNumber != ""

fun AccountData.mergeWith(accountData: AccountData): AccountData {
    authRequired = accountData.authRequired
    keyAlias = accountData.keyAlias
    return this
}