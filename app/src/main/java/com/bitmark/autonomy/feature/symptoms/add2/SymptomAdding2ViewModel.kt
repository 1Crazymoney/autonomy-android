/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.add2

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.SymptomRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.ext.append
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.SymptomModelView
import com.bitmark.autonomy.util.modelview.SymptomType
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class SymptomAdding2ViewModel(
    lifecycle: Lifecycle,
    private val symptomRepo: SymptomRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listSymptomLiveData = CompositeLiveData<List<SymptomModelView>>()

    internal val searchSymptomLiveData = CompositeLiveData<Pair<List<SymptomModelView>, String>>()

    internal val addNewSymptomLiveData = CompositeLiveData<SymptomModelView>()

    fun listSymptom(lang: String) {
        listSymptomLiveData.add(
            rxLiveDataTransformer.flowable(
                symptomRepo.listAllSymptom(
                    lang
                ).map { p ->
                    val officialSymptoms = p.first.map { s -> SymptomModelView.newInstance(s) }
                    val customizedSymptoms = p.second.map { s -> SymptomModelView.newInstance(s) }
                    val suggestedSymptoms = p.third.map { s -> SymptomModelView.newInstance(s) }
                    officialSymptoms.toMutableList().append(customizedSymptoms).toMutableList()
                        .append(suggestedSymptoms)
                })
        )
    }

    fun search(symptoms: List<SymptomModelView>, searchText: String) {
        searchSymptomLiveData.add(
            rxLiveDataTransformer.single(
                Single.fromCallable {
                    Pair(symptoms.filter { s -> s.name.contains(searchText, true) }, searchText)
                }.subscribeOn(
                    Schedulers.computation()
                )
            )
        )
    }

    fun addSymptom(name: String) {
        addNewSymptomLiveData.add(
            rxLiveDataTransformer.single(
                symptomRepo.addSymptom(
                    name,
                    ""
                ).map { id -> SymptomModelView(id, name, "", SymptomType.NEIGHBORHOOD) })
        )
    }
}