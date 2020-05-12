/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.source.local.SymptomLocalDataSource
import com.bitmark.autonomy.data.source.remote.SymptomRemoteDataSource
import io.reactivex.Maybe


class SymptomRepository(
    private val remoteDataSource: SymptomRemoteDataSource,
    private val localDataSource: SymptomLocalDataSource
) : Repository {

    fun listSymptom(lang: String) = remoteDataSource.listSymptom(lang)

    fun listAllSymptom(lang: String) = Maybe.merge(
        remoteDataSource.listAllSymptom(lang).flatMapMaybe { t ->
            localDataSource.saveSymptoms(t).andThen(Maybe.just(t))
        }, localDataSource.listSymptom().onErrorComplete()
    )

    fun reportSymptom(ids: List<String>) = remoteDataSource.reportSymptom(ids)

    fun addSymptom(name: String, desc: String) = remoteDataSource.addSymptom(name, desc)

    fun listSymptomHistory(beforeSec: Long? = null, lang: String, limit: Int = 20) =
        remoteDataSource.listSymptomHistory(beforeSec, lang, limit)

    fun getSymptomMetric() = remoteDataSource.getSymptomMetric()

}