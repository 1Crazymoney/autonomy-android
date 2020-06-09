/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.rating

import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class ResourceRatingModule {

    @ActivityScope
    @Provides
    fun provideNav(activity: ResourceRatingActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideVM(
        activity: ResourceRatingActivity,
        userRepo: UserRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = ResourceRatingViewModel(activity.lifecycle, userRepo, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: ResourceRatingActivity) = DialogController(activity)
}