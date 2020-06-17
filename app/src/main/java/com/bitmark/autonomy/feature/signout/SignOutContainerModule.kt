/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.signout

import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.Navigator
import dagger.Module
import dagger.Provides

@Module
class SignOutContainerModule {

    @Provides
    @ActivityScope
    fun provideNavigator(activity: SignOutContainerActivity) = Navigator(activity)
}