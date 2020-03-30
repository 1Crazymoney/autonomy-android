/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.survey

import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.BehaviorComponent
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.survey.checkin.SurveyCheckin1Fragment
import javax.inject.Inject


class SurveyContainerActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_survey_container

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        navigator.anim(NONE)
            .replaceFragment(R.id.layoutContainer, SurveyCheckin1Fragment.newInstance(), false)
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.layoutContainer)
        if ((currentFragment as? BehaviorComponent)?.onBackPressed() == false) {
            navigator.anim(Navigator.RIGHT_LEFT).finishActivity()
            super.onBackPressed()
        }
    }
}