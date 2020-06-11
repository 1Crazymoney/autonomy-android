/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.addresource.select

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.ResourceRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.ResourceModelView


class SelectResourceViewModel(
    lifecycle: Lifecycle,
    private val resourceRepo: ResourceRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listImportantResourceLiveData = CompositeLiveData<List<ResourceModelView>>()

    internal val addNewResourceLiveData = CompositeLiveData<List<ResourceModelView>>()

    fun listImportantResource(poiId: String, lang: String) {
        listImportantResourceLiveData.add(
            rxLiveDataTransformer.single(
                resourceRepo.listImportantResource(poiId, lang).map { res ->
                    res.map { r ->
                        ResourceModelView.newInstance(
                            r
                        )
                    }
                })
        )
    }

    fun addResource(poiId: String, existingResIds: List<String>, newResNames: List<String>) {
        addNewResourceLiveData.add(
            rxLiveDataTransformer.single(
                resourceRepo.addResources(
                    poiId,
                    existingResIds,
                    newResNames
                ).map { res -> res.map { r -> ResourceModelView(r.id, r.name) } })
        )
    }

}