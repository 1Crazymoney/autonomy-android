/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.trending

import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.di.FragmentScope
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class TrendingModule {

    @Provides
    @FragmentScope
    fun provideNav(fragment: TrendingFragment) = Navigator(fragment.activity!!)

    @Provides
    @FragmentScope
    fun provideVM(
        fragment: TrendingFragment,
        appRepo: AppRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = TrendingViewModel(fragment.lifecycle, appRepo, rxLiveDataTransformer)
}