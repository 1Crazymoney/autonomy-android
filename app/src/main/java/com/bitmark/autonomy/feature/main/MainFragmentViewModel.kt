/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.AreaProfileModelView
import com.bitmark.autonomy.util.modelview.FormulaModelView


class MainFragmentViewModel(
    lifecycle: Lifecycle,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val getAreaProfileLiveData = CompositeLiveData<AreaProfileModelView>()

    internal val getFormulaLiveData = CompositeLiveData<FormulaModelView>()

    internal val deleteFormulaLiveData = CompositeLiveData<FormulaModelView>()

    internal val updateFormulaLiveData = CompositeLiveData<Any>()

    fun getCurrentAreaProfile() {
        getAreaProfileLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getCurrentAreaProfile().map { a -> AreaProfileModelView.newInstance(a) }
            )
        )
    }

    fun getAreaProfile(id: String) {
        getAreaProfileLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getAreaProfile(id).map { a -> AreaProfileModelView.newInstance(a) }
            )
        )
    }

    fun getFormula() {
        getFormulaLiveData.add(rxLiveDataTransformer.single(userRepo.getFormula().map { f ->
            FormulaModelView.newInstance(f)
        }))
    }

    fun deleteFormula() {
        deleteFormulaLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.deleteFormula().andThen(
                    userRepo.getFormula().map { f -> FormulaModelView.newInstance(f) })
            )
        )
    }

    fun updateFormula(formula: FormulaModelView) {
        updateFormulaLiveData.add(
            rxLiveDataTransformer.completable(
                userRepo.updateFormula(formula.toCoefficientData())
            )
        )
    }
}