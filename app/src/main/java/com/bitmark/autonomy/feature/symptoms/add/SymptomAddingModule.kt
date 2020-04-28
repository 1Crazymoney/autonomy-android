/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.add

import com.bitmark.autonomy.data.source.SymptomRepository
import com.bitmark.autonomy.di.FragmentScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class SymptomAddingModule {

    @Provides
    @FragmentScope
    fun provideNav(fragment: SymptomAddingFragment) = Navigator(fragment)

    @Provides
    @FragmentScope
    fun provideVM(
        fragment: SymptomAddingFragment,
        symptomRepo: SymptomRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) =
        SymptomAddingViewModel(fragment.lifecycle, symptomRepo, rxLiveDataTransformer)

    @Provides
    @FragmentScope
    fun provideDialogController(fragment: SymptomAddingFragment) =
        DialogController(fragment.activity!!)
}