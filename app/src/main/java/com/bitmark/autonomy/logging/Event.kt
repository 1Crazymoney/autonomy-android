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

    ACCOUNT_UPDATE_TIMEZONE_ERROR("account_update_timezone_error"),

    APP_GET_INFO_ERROR("app_get_info_error"),

    SHARE_PREF_ERROR("shared_pref_error"),

    LOCATION_PERMISSION_GRANTED("location_permission_granted"),

    LOCATION_PERMISSION_DENIED("location_permission_denied"),

    LOCATION_SETTING_CANNOT_BE_RESOLVED("location_setting_cannot_be_resolved"),

    LOCATION_SETTING_NEED_TO_BE_RESOLVED("location_setting_need_to_be_resolved"),

    HELP_REQUEST_LISTING_ERROR("help_request_listing_error"),

    HELP_REQUEST_GETTING_ERROR("help_request_getting_error"),

    HELP_REQUEST_RESPOND_ERROR("help_request_respond_error"),

    SYMPTOM_LISTING_ERROR("symptom_listing_error"),

    SYMPTOM_AUTOCOMPLETE_LISTING_ERROR("symptom_autocomplete_listing_error"),

    SYMPTOM_AUTOCOMPLETE_SEARCHING_ERROR("symptom_autocomplete_searching_error"),

    SYMPTOM_REPORT_ERROR("symptom_report_error"),

    SYMPTOM_ADDING_ERROR("symptom_adding_error"),

    SYMPTOM_METRIC_ERROR("symptom_metric_error"),

    NOTIFICATION_HANDLING_ERROR("notification_handling_error"),

    AREA_PROFILE_GETTING_ERROR("area_profile_getting_error"),

    BEHAVIOR_LISTING_ERROR("behavior_listing_error"),

    BEHAVIOR_REPORT_ERROR("behavior_report_error"),

    BEHAVIOR_ADDING_ERROR("behavior_adding_error"),

    BEHAVIOR_AUTOCOMPLETE_SEARCHING_ERROR("behavior_autocomplete_searching_error"),

    BEHAVIOR_AUTOCOMPLETE_LISTING_ERROR("behavior_autocomplete_listing_error"),

    BEHAVIOR_METRIC_ERROR("behavior_metric_error"),

    AREA_DELETE_ERROR("area_delete_error"),

    AREA_RENAME_ERROR("area_rename_error"),

    AREA_LIST_ERROR("area_list_error"),

    AREA_ADDING_ERROR("area_adding_error"),

    FORMULA_GETTING_ERROR("formula_getting_error"),

    FORMULA_UPDATE_ERROR("formula_update_error"),

    FORMULA_DELETE_ERROR("formula_delete_error"),

    DEBUG_INFO_GETTING_ERROR("debug_info_getting_error"),

    VIDEO_PLAYING_ERROR("video_playing_error"),

    LOCATION_BACKGROUND_UPDATE_ERROR("location_background_update_error"),

    RESOURCE_RATING_LISTING_ERROR("resource_rating_listing_error"),

    RESOURCE_RATING_UPDATING_ERROR("resource_rating_updating_error"),

    RESOURCE_ADDING_ERROR("resource_adding_error"),

    RESOURCE_AUTOCOMPLETE_LISTING_ERROR("resource_autocomplete_listing_error"),

    RESOURCE_LISTING_ERROR("resource_listing_error"),

    RESOURCE_AUTOCOMPLETE_SEARCHING_ERROR("resource_autocomplete_searching_error"),

}