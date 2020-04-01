/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.source.remote.SymptomRemoteDataSource


class SymptomRepository(private val remoteDataSource: SymptomRemoteDataSource) : Repository {

    fun listSymptom() = remoteDataSource.listSymptom()

    fun reportSymptom(ids: List<String>) = remoteDataSource.reportSymptom(ids)
}