/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.signout.signout

import com.bitmark.autonomy.data.source.AccountRepository
import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.di.FragmentScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class SignOutModule {

    @Provides
    @FragmentScope
    fun provideNavigator(fragment: SignOutFragment) = Navigator(fragment)

    @Provides
    @FragmentScope
    fun provideViewModel(
        fragment: SignOutFragment,
        appRepo: AppRepository,
        accountRepo: AccountRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = SignOutViewModel(fragment.lifecycle, appRepo, accountRepo, rxLiveDataTransformer)

    @Provides
    @FragmentScope
    fun provideDialogController(fragment: SignOutFragment) = DialogController(fragment.activity!!)
}