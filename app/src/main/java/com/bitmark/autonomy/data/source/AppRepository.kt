/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.source.local.AppLocalDataSource
import com.bitmark.autonomy.data.source.remote.AppRemoteDataSource


class AppRepository(
    private val remoteDataSource: AppRemoteDataSource,
    private val localDataSource: AppLocalDataSource
) : Repository {

    fun getAppInfo() = remoteDataSource.getAppInfo()

    fun checkDebugModeEnable() = localDataSource.checkDebugModeEnable()

    fun saveDebugModeState(enable: Boolean) = localDataSource.saveDebugModeState(enable)

    fun listScore(addresses: List<String>) = remoteDataSource.listScore(addresses)

    fun listReportItem(
        scope: String,
        type: String,
        start: Long,
        end: Long,
        lang: String,
        poiId: String? = null
    ) = remoteDataSource.listReportItem(scope, type, start, end, lang, poiId)
}