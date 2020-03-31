/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.survey.checkin

import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.FADE_IN
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.main.MainActivity
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import kotlinx.android.synthetic.main.fragment_survey_checkin_1.*
import javax.inject.Inject


class SurveyCheckin1Fragment : BaseSupportFragment() {

    companion object {
        fun newInstance() = SurveyCheckin1Fragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.fragment_survey_checkin_1

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        ivRed.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT)
                .replaceFragment(R.id.layoutContainer, SurveyCheckin2Fragment.newInstance(), true)
        }

        ivYellow.setSafetyOnclickListener {
            navigator.anim(FADE_IN).startActivityAsRoot(MainActivity::class.java)
        }

        ivGreen.setSafetyOnclickListener {
            navigator.anim(FADE_IN).startActivityAsRoot(MainActivity::class.java)
        }
    }

    override fun onBackPressed(): Boolean {
        return navigator.popFragment()
    }
}