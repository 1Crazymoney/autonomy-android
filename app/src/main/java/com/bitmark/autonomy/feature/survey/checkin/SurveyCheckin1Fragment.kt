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
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.behavior.BehaviorReportActivity
import com.bitmark.autonomy.feature.symptoms.SymptomReportActivity
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
            navigator.anim(RIGHT_LEFT).startActivity(SymptomReportActivity::class.java)
            navigator.anim(NONE).finishActivity()
        }

        ivYellow.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(BehaviorReportActivity::class.java)
            navigator.anim(NONE).finishActivity()
        }

        ivGreen.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(BehaviorReportActivity::class.java)
            navigator.anim(NONE).finishActivity()
        }
    }

    override fun onBackPressed(): Boolean {
        return navigator.popFragment()
    }
}