/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.source.remote.UserRemoteDataSource


class UserRepository(private val remoteDataSource: UserRemoteDataSource) : Repository {

    fun getHealthScore() = remoteDataSource.getHealthScore()

    fun addArea(alias: String, address: String, lat: Double, lng: Double) =
        remoteDataSource.addArea(alias, address, lat, lng)

    fun deleteArea(id: String) = remoteDataSource.deleteArea(id)

    fun listArea() = remoteDataSource.listArea()

    fun reorderArea(ids: List<String>) = remoteDataSource.reorderArea(ids)

    fun rename(id: String, name: String) = remoteDataSource.rename(id, name)
}