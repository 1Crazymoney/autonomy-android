/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.addresource.add

import com.bitmark.autonomy.data.source.ResourceRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class AddResourceModule {

    @Provides
    @ActivityScope
    fun provideNav(activity: AddResourceActivity) = Navigator(activity)

    @Provides
    @ActivityScope
    fun provideDialogController(activity: AddResourceActivity) = DialogController(activity)

    @Provides
    @ActivityScope
    fun provideVM(
        activity: AddResourceActivity,
        resourceRepo: ResourceRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = AddResourceViewModel(activity.lifecycle, resourceRepo, rxLiveDataTransformer)
}