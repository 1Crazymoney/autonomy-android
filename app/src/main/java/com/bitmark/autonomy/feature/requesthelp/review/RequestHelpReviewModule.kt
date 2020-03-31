/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.requesthelp.review

import com.bitmark.autonomy.data.source.AssistanceRepository
import com.bitmark.autonomy.di.FragmentScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class RequestHelpReviewModule {

    @Provides
    @FragmentScope
    fun provideNav(fragment: RequestHelpReviewFragment) = Navigator(fragment)

    @Provides
    @FragmentScope
    fun provideVM(
        fragment: RequestHelpReviewFragment,
        assistanceRepo: AssistanceRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) =
        RequestHelpReviewViewModel(fragment.lifecycle, assistanceRepo, rxLiveDataTransformer)

    @Provides
    @FragmentScope
    fun provideDialogController(fragment: RequestHelpReviewFragment) =
        DialogController(fragment.activity!!)
}