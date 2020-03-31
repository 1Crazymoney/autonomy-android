/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.respondhelp

import com.bitmark.autonomy.data.source.AssistanceRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class RespondHelpModule {

    @Provides
    @ActivityScope
    fun provideNav(activity: RespondHelpActivity) = Navigator(activity)

    @Provides
    @ActivityScope
    fun provideVM(
        activity: RespondHelpActivity,
        assistanceRepo: AssistanceRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = RespondHelpViewModel(activity.lifecycle, assistanceRepo, rxLiveDataTransformer)

    @Provides
    @ActivityScope
    fun provideDialogController(activity: RespondHelpActivity) = DialogController(activity)

}