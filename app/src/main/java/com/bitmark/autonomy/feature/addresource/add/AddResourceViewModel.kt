/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.addresource.add

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.ResourceRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.ResourceModelView
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class AddResourceViewModel(
    lifecycle: Lifecycle,
    private val resourceRepo: ResourceRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listResourceLiveData = CompositeLiveData<List<ResourceModelView>>()

    internal val searchResourceLiveData = CompositeLiveData<Pair<List<ResourceModelView>, String>>()

    fun listResource(poiId: String, lang: String) {
        listResourceLiveData.add(
            rxLiveDataTransformer.flowable(
                resourceRepo.listResource(
                    poiId,
                    lang,
                    true
                ).map { res -> res.map { r -> ResourceModelView.newInstance(r) } })
        )
    }

    fun search(resources: List<ResourceModelView>, searchText: String) {
        searchResourceLiveData.add(
            rxLiveDataTransformer.single(
                Single.fromCallable {
                    Pair(resources.filter { s -> s.name.contains(searchText, true) }, searchText)
                }.subscribeOn(
                    Schedulers.computation()
                )
            )
        )
    }
}