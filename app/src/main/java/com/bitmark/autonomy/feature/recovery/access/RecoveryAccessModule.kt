/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.recovery.access

import com.bitmark.autonomy.di.FragmentScope
import com.bitmark.autonomy.feature.Navigator
import dagger.Module
import dagger.Provides

@Module
class RecoveryAccessModule {

    @Provides
    @FragmentScope
    fun provideNavigator(fragment: RecoveryAccessFragment) = Navigator(fragment)
}