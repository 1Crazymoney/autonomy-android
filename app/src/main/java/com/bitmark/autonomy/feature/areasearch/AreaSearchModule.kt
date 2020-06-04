/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.areasearch

import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class AreaSearchModule {

    @Provides
    @ActivityScope
    fun provideNav(activity: AreaSearchActivity) = Navigator(activity)

    @Provides
    @ActivityScope
    fun provideDialogController(activity: AreaSearchActivity) = DialogController(activity)

    @Provides
    @ActivityScope
    fun provideVM(
        activity: AreaSearchActivity,
        userRepo: UserRepository,
        appRepo: AppRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = AreaSearchViewModel(
        activity.lifecycle,
        userRepo,
        appRepo,
        rxLiveDataTransformer
    )

}