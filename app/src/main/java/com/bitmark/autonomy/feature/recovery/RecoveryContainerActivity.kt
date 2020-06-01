/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.recovery

import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.BehaviorComponent
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.recovery.notice.RecoveryNoticeFragment
import javax.inject.Inject


class RecoveryContainerActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_recovery_container

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        navigator.anim(NONE)
            .replaceFragment(R.id.layoutRoot, RecoveryNoticeFragment.newInstance(), false)
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.layoutRoot)
        if (currentFragment != null && currentFragment is BehaviorComponent) {
            currentFragment.onBackPressed()
        } else {
            navigator.anim(Navigator.RIGHT_LEFT).finishActivity()
        }
    }
}