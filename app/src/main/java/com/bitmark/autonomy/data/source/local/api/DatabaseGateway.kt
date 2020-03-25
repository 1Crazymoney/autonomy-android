/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local.api

import com.bitmark.autonomy.BuildConfig

abstract class DatabaseGateway {

    companion object {
        const val DATABASE_NAME = BuildConfig.APPLICATION_ID
    }

}