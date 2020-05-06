/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.profile

import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class ProfileModule {

    @Provides
    @ActivityScope
    fun provideNav(activity: ProfileActivity) = Navigator(activity)

    @Provides
    @ActivityScope
    fun provideVM(
        activity: ProfileActivity,
        appRepo: AppRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = ProfileViewModel(activity.lifecycle, appRepo, rxLiveDataTransformer)

}