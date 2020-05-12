/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.source.local.BehaviorLocalDataSource
import com.bitmark.autonomy.data.source.remote.BehaviorRemoteDataSource
import io.reactivex.Maybe


class BehaviorRepository(
    private val remoteDataSource: BehaviorRemoteDataSource,
    private val localDataSource: BehaviorLocalDataSource
) : Repository {

    fun listBehavior(lang: String) = remoteDataSource.listBehavior(lang)

    fun listAllBehavior(lang: String) = Maybe.merge(
        remoteDataSource.listAllBehavior(lang).flatMapMaybe { t ->
            localDataSource.saveBehaviors(t).andThen(Maybe.just(t))
        }, localDataSource.listBehavior().onErrorComplete()
    )

    fun reportBehaviors(ids: List<String>) = remoteDataSource.reportBehaviors(ids)

    fun addBehavior(name: String, desc: String) = remoteDataSource.addBehavior(name, desc)

    fun listBehaviorHistory(beforeSec: Long? = null, lang: String, limit: Int = 20) =
        remoteDataSource.listBehaviorHistory(beforeSec, lang, limit)

    fun getBehaviorMetric() = remoteDataSource.getBehaviorMetric()
}