/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


data class AccountData(

    @Expose
    @SerializedName("id")
    val id: String,

    @Expose
    @SerializedName("account_number")
    val accountNumber: String,

    @Expose
    @SerializedName("created_at")
    val createdAt: String,

    @Expose
    @SerializedName("updated_at")
    val updatedAt: String,

    @Expose
    @SerializedName("metadata")
    val metadata: Map<String, String>?,

    @Expose
    @SerializedName("state")
    val state: State?,

    @Expose
    @SerializedName("auth_required")
    var authRequired: Boolean = false,

    @Expose
    @SerializedName("key_alias")
    var keyAlias: String
) : Data {
    companion object
}

data class State(
    @Expose
    @SerializedName("last_active_time")
    val lastActiveTime: String,

    @Expose
    @SerializedName("location")
    val location: Location?
) : Data

@Parcelize
data class Location(
    @Expose
    @SerializedName("latitude")
    val lat: Double,

    @Expose
    @SerializedName("longitude")
    val lng: Double
) : Data, Parcelable {
    override fun toString(): String {
        return "(%f, %f)".format(lat, lng)
    }
}

fun AccountData.Companion.newInstance(): AccountData =
    AccountData("", "", "", "", null, null, false, "")

fun AccountData.isRegistered(): Boolean = accountNumber != ""

fun AccountData.mergeWith(accountData: AccountData): AccountData {
    authRequired = accountData.authRequired
    keyAlias = accountData.keyAlias
    return this
}