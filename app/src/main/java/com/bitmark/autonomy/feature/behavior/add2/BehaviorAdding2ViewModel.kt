/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.add2

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.BehaviorRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.ext.append
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.BehaviorModelView
import com.bitmark.autonomy.util.modelview.BehaviorType
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class BehaviorAdding2ViewModel(
    lifecycle: Lifecycle,
    private val behaviorRepo: BehaviorRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listBehaviorLiveData = CompositeLiveData<List<BehaviorModelView>>()

    internal val searchBehaviorLiveData = CompositeLiveData<Pair<List<BehaviorModelView>, String>>()

    internal val addNewBehaviorLiveData = CompositeLiveData<BehaviorModelView>()

    fun listBehavior(lang: String) {
        listBehaviorLiveData.add(
            rxLiveDataTransformer.flowable(
                behaviorRepo.listAllBehavior(
                    lang
                ).map { p ->
                    val officialBehaviors = p.first.map { s -> BehaviorModelView.newInstance(s) }
                    val customizedBehaviors = p.second.map { s -> BehaviorModelView.newInstance(s) }
                    officialBehaviors.toMutableList().append(customizedBehaviors)
                })
        )
    }

    fun search(behaviors: List<BehaviorModelView>, searchText: String) {
        searchBehaviorLiveData.add(
            rxLiveDataTransformer.single(
                Single.fromCallable {
                    Pair(behaviors.filter { s -> s.name.contains(searchText, true) }, searchText)
                }.subscribeOn(
                    Schedulers.computation()
                )
            )
        )
    }

    fun addBehavior(name: String) {
        addNewBehaviorLiveData.add(
            rxLiveDataTransformer.single(
                behaviorRepo.addBehavior(
                    name,
                    ""
                ).map { id -> BehaviorModelView(id, name, "", BehaviorType.NEIGHBORHOOD) })
        )
    }
}