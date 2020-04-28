/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.add

import com.bitmark.autonomy.data.source.BehaviorRepository
import com.bitmark.autonomy.di.FragmentScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class BehaviorAddingModule {

    @Provides
    @FragmentScope
    fun provideNav(fragment: BehaviorAddingFragment) = Navigator(fragment)

    @Provides
    @FragmentScope
    fun provideVM(
        fragment: BehaviorAddingFragment,
        behaviorRepo: BehaviorRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) =
        BehaviorAddingViewModel(fragment.lifecycle, behaviorRepo, rxLiveDataTransformer)

    @Provides
    @FragmentScope
    fun provideDialogController(fragment: BehaviorAddingFragment) =
        DialogController(fragment.activity!!)
}