/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.areasearch

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.data.source.ResourceRepository
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.modelview.ResourceModelView


class AreaSearchViewModel(
    lifecycle: Lifecycle,
    private val resourceRepo: ResourceRepository,
    private val appRepo: AppRepository,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listResourceLiveData = CompositeLiveData<List<ResourceModelView>>()

    internal val listPlaceLiveData = CompositeLiveData<List<AreaModelView>>()

    internal val createAreaLiveData = CompositeLiveData<AreaModelView>()

    fun listResources(lang: String) {
        listResourceLiveData.add(
            rxLiveDataTransformer.single(
                resourceRepo.listSuggestedResource(lang).map { res ->
                    res.map { r ->
                        ResourceModelView.newInstance(
                            r
                        )
                    }
                })
        )
    }

    fun listPlace(resourceId: String) {
        listPlaceLiveData.add(rxLiveDataTransformer.single(appRepo.listArea(resourceId).map { places ->
            places.map { a ->
                AreaModelView.newInstance(a)
            }
        }))
    }

    fun createArea(alias: String, address: String, lat: Double, lng: Double) {
        createAreaLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.createArea(
                    alias,
                    address,
                    lat,
                    lng
                ).map { AreaModelView.newInstance(it) })
        )
    }

}