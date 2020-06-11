/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.model.CoefficientData
import com.bitmark.autonomy.data.source.remote.UserRemoteDataSource


class UserRepository(private val remoteDataSource: UserRemoteDataSource) : Repository {

    fun getHealthScore() = remoteDataSource.getHealthScore()

    fun addArea(alias: String, address: String, lat: Double, lng: Double) =
        remoteDataSource.addArea(alias, address, lat, lng)

    fun deleteArea(id: String) = remoteDataSource.deleteArea(id)

    fun listArea() = remoteDataSource.listArea()

    fun reorderArea(ids: List<String>) = remoteDataSource.reorderArea(ids)

    fun rename(id: String, name: String) = remoteDataSource.rename(id, name)

    fun getYourAutonomyProfile() = remoteDataSource.getYourAutonomyProfile()

    fun getAutonomyProfile(id: String, allResource: Boolean = false) =
        remoteDataSource.getAutonomyProfile(id, allResource)

    fun listLocationHistory(beforeSec: Long? = null, limit: Int = 20) =
        remoteDataSource.listLocationHistory(beforeSec, limit)

    fun getFormula(lang: String) = remoteDataSource.getFormula(lang)

    fun deleteFormula() = remoteDataSource.deleteFormula()

    fun updateFormula(coefficientData: CoefficientData) =
        remoteDataSource.updateFormula(coefficientData)

    fun getDebugInfo() = remoteDataSource.getDebugInfo()

    fun getDebugInfo(id: String) = remoteDataSource.getDebugInfo(id)

    fun updateLocation() = remoteDataSource.updateLocation()
}