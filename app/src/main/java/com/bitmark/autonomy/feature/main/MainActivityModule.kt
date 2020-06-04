/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import com.bitmark.autonomy.data.source.AccountRepository
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.auth.ServerAuthentication
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class MainActivityModule {

    @Provides
    @ActivityScope
    fun provideNav(activity: MainActivity) = Navigator(activity)

    @Provides
    @ActivityScope
    fun provideDialogController(activity: MainActivity) = DialogController(activity)

    @Provides
    @ActivityScope
    fun provideVM(
        activity: MainActivity,
        userRepo: UserRepository,
        accountRepo: AccountRepository,
        rxLiveDataTransformer: RxLiveDataTransformer,
        serverAuth: ServerAuthentication
    ) = MainActivityViewModel(
        activity.lifecycle,
        userRepo,
        accountRepo,
        rxLiveDataTransformer,
        serverAuth
    )
}