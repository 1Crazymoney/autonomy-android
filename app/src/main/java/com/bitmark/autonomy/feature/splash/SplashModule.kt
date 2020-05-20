/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.splash

import com.bitmark.autonomy.data.source.AccountRepository
import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class SplashModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: SplashActivity,
        accountRepo: AccountRepository,
        appRepo: AppRepository,
        userRepo: UserRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = SplashViewModel(activity.lifecycle, accountRepo, appRepo, userRepo, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNav(activity: SplashActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: SplashActivity) = DialogController(activity)

}