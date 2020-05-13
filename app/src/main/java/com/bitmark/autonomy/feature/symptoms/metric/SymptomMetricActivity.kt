/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.metric

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.behavior.BehaviorReportActivity
import com.bitmark.autonomy.feature.guidance.GuidanceActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.SymptomMetricModelView
import kotlinx.android.synthetic.main.activity_symptom_metric.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt


class SymptomMetricActivity : BaseAppCompatActivity() {

    companion object {
        private const val BEHAVIOR_REPORT_REQUEST_CODE = 0x01
    }

    @Inject
    internal lateinit var viewModel: SymptomMetricViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    override fun layoutRes(): Int = R.layout.activity_symptom_metric

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getMetric()
    }

    override fun initComponents() {
        super.initComponents()

        layoutExtNavigation.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(GuidanceActivity::class.java)
        }

        layoutReportBehavior.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivityForResult(
                BehaviorReportActivity::class.java,
                BEHAVIOR_REPORT_REQUEST_CODE
            )
        }

        layoutDone.setSafetyOnclickListener { navigator.anim(BOTTOM_UP).finishActivity() }
    }

    override fun observe() {
        super.observe()

        viewModel.getSymptomMetricLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data()!!
                    showData(data)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.SYMPTOM_METRIC_ERROR, res.throwable())
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })
    }

    private fun showData(data: SymptomMetricModelView) {
        tvTotalToday.text = data.mine.totalToday.toString()
        val myTodayDelta = data.mine.delta
        tvTotalTodayChange.text = String.format("%.2f%%", abs(myTodayDelta))
        when {
            myTodayDelta == 0f -> {
                ivTotalTodayChange.invisible()
                tvTotalTodayChange.setTextColorRes(R.color.white)
            }
            myTodayDelta < 0f -> {
                ivTotalTodayChange.visible()
                tvTotalTodayChange.setTextColorRes(R.color.apple)
                ivTotalTodayChange.setImageResource(R.drawable.ic_down_green)
            }
            myTodayDelta > 0f -> {
                ivTotalTodayChange.visible()
                tvTotalTodayChange.setTextColorRes(R.color.persian_red)
                ivTotalTodayChange.setImageResource(R.drawable.ic_up_red)
            }
        }

        tvAvgToday.text = data.community.avgToday.roundToInt().toString()
        val avgDelta = data.community.delta
        tvAvgTodayChange.text = String.format("%.2f%%", abs(avgDelta))
        when {
            avgDelta == 0f -> {
                ivAvgTodayChange.invisible()
                tvAvgTodayChange.setTextColorRes(R.color.white)
            }
            avgDelta < 0f -> {
                ivAvgTodayChange.visible()
                tvAvgTodayChange.setTextColorRes(R.color.apple)
                ivAvgTodayChange.setImageResource(R.drawable.ic_down_green)
            }
            avgDelta > 0f -> {
                ivAvgTodayChange.visible()
                tvAvgTodayChange.setTextColorRes(R.color.persian_red)
                ivAvgTodayChange.setImageResource(R.drawable.ic_up_red)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == BEHAVIOR_REPORT_REQUEST_CODE) {
            navigator.anim(NONE).finishActivity()
        }
    }

    override fun onBackPressed() {
        navigator.anim(BOTTOM_UP).finishActivity()
        super.onBackPressed()
    }
}