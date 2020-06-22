/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.autonomyprofile

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.model.Location
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.modelview.AutonomyProfileModelView


class AutonomyProfileViewModel(
    lifecycle: Lifecycle,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val getAutonomyProfileLiveData = CompositeLiveData<AutonomyProfileModelView>()

    internal val addAreaLiveData = CompositeLiveData<AreaModelView>()

    fun getAutonomyProfile(
        poiId: String? = null,
        allResources: Boolean? = null,
        lang: String? = null,
        me: Boolean? = null,
        lat: Double? = null,
        lng: Double? = null
    ) {
        getAutonomyProfileLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getAutonomyProfile(
                    poiId,
                    allResources,
                    lang,
                    me,
                    lat,
                    lng
                ).map { a -> AutonomyProfileModelView.newInstance(a) }
            )
        )
    }

    fun addArea(alias: String, address: String, location: Location) {
        addAreaLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.addArea(
                    alias,
                    address,
                    location.lat,
                    location.lng
                ).map { a -> AreaModelView(a.id, a.alias, a.location, a.score) })
        )
    }

}