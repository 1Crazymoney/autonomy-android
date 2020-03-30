/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.logging

enum class Event(val value: String) {

    ACCOUNT_JWT_ERROR("register_jwt_error"),

    ACCOUNT_LOAD_KEY_STORE_ERROR("account_load_keystore_error"),

    ACCOUNT_SAVE_TO_KEY_STORE_ERROR("account_save_keystore_error"),

    ACCOUNT_REGISTER_ERROR("account_register_error"),

    APP_GET_INFO_ERROR("app_get_info_error"),

    SHARE_PREF_ERROR("shared_pref_error"),

    LOCATION_PERMISSION_GRANTED("location_permission_granted"),

    LOCATION_PERMISSION_DENIED("location_permission_denied"),

    LOCATION_SETTING_CANNOT_BE_RESOLVED("location_setting_cannot_be_resolved"),

    LOCATION_SETTING_NEED_TO_BE_RESOLVED("location_setting_need_to_be_resolved")

}