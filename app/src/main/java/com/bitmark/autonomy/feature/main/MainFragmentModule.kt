/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.di.FragmentScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class MainFragmentModule {

    @Provides
    @FragmentScope
    fun provideNav(fragment: MainFragment) = Navigator(fragment)

    @Provides
    @FragmentScope
    fun provideDialogController(fragment: MainFragment) = DialogController(fragment.activity!!)

    @Provides
    @FragmentScope
    fun provideVM(
        fragment: MainFragment,
        userRepo: UserRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = MainFragmentViewModel(
        fragment.lifecycle,
        userRepo,
        rxLiveDataTransformer
    )
}