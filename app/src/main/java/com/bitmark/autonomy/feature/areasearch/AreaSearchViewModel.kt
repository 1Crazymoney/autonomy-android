/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.areasearch

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.model.Location
import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.location.PlaceAutoComplete
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.AreaModelView


class AreaSearchViewModel(
    lifecycle: Lifecycle,
    private val userRepo: UserRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val addAreaLiveData = CompositeLiveData<AreaModelView>()

    internal val listScoreLiveData = CompositeLiveData<List<PlaceAutoComplete>>()

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

    fun listScore(places: List<PlaceAutoComplete>) {
        listScoreLiveData.add(
            rxLiveDataTransformer.single(
                appRepo.listScore(places.map { p -> p.secondaryText }).map { scores ->
                    scores.forEachIndexed { i, s -> places[i].score = s }
                    places
                }
            )
        )
    }

}