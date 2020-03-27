/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local

import com.bitmark.autonomy.data.source.local.api.DatabaseApi
import com.bitmark.autonomy.data.source.local.api.FileStorageApi
import com.bitmark.autonomy.data.source.local.api.SharedPrefApi
import javax.inject.Inject


class AppLocalDataSource @Inject constructor(
    databaseApi: DatabaseApi,
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : LocalDataSource(databaseApi, sharedPrefApi, fileStorageApi) {
}