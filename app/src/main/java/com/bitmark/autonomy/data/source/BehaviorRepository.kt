/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.source.remote.BehaviorRemoteDataSource


class BehaviorRepository(private val remoteDataSource: BehaviorRemoteDataSource) : Repository {

    fun listBehavior() = remoteDataSource.listBehavior()

    fun reportBehaviors(ids: List<String>) = remoteDataSource.reportBehaviors(ids)

    fun addBehavior(name: String, desc: String) = remoteDataSource.addBehavior(name, desc)

    fun listBehaviorHistory(beforeSec: Long? = null, limit: Int = 20) =
        remoteDataSource.listBehaviorHistory(beforeSec, limit)
}