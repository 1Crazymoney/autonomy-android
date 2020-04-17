/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.survey.checkin

import android.app.Activity
import android.content.Intent
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

        private const val SYMPTOM_REPORT_REQUEST_CODE = 0x01

        private const val BEHAVIOR_REPORT_REQUEST_CODE = 0x02
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.fragment_survey_checkin_1

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        ivRed.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivityForResult(
                SymptomReportActivity::class.java,
                SYMPTOM_REPORT_REQUEST_CODE
            )
        }

        ivYellow.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivityForResult(
                BehaviorReportActivity::class.java,
                BEHAVIOR_REPORT_REQUEST_CODE
            )
        }

        ivGreen.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivityForResult(
                BehaviorReportActivity::class.java,
                BEHAVIOR_REPORT_REQUEST_CODE
            )
        }
    }

    override fun onBackPressed(): Boolean {
        return navigator.popFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SYMPTOM_REPORT_REQUEST_CODE, BEHAVIOR_REPORT_REQUEST_CODE -> navigator.anim(NONE).finishActivity()
                else -> error("unsupported request code: $requestCode")
            }
        }
    }
}