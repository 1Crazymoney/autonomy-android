/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.trending

import android.os.Bundle
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.ReportScope
import com.bitmark.autonomy.data.model.ReportType
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.behavior.BehaviorReportActivity
import com.bitmark.autonomy.feature.symptoms.SymptomReportActivity
import com.bitmark.autonomy.util.ChromeCustomTabServiceHandler
import com.bitmark.autonomy.util.Constants.JUPYTER_NOTEBOOK_URL
import com.bitmark.autonomy.util.ext.invisible
import com.bitmark.autonomy.util.ext.openChromeTab
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.ext.visible
import kotlinx.android.synthetic.main.activity_trending_container.*
import javax.inject.Inject


class TrendingContainerActivity : BaseAppCompatActivity() {

    companion object {
        private const val TYPE = "type"

        private const val SCOPE = "scope"

        private const val POI_ID = "poi_id"

        fun getBundle(type: String, scope: String, poiId: String? = null) = Bundle().apply {
            putString(TYPE, type)
            putString(SCOPE, scope)
            if (poiId != null) putString(POI_ID, poiId)
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var customTabServiceHandler: ChromeCustomTabServiceHandler

    private lateinit var vpAdapter: TrendingViewPagerAdapter

    override fun layoutRes(): Int = R.layout.activity_trending_container

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val scope = intent?.extras?.getString(SCOPE) ?: error("missing scope")
        val type = intent?.extras?.getString(TYPE) ?: error("missing type")
        val poiId = intent?.extras?.getString(POI_ID)
        if (scope == ReportScope.POI.value && poiId == null) error("missing poi_id")

        when (type) {
            ReportType.SCORE.value -> {
                tvAction.setText(R.string.view_on_jupyter)
                ivAction.setImageResource(R.drawable.ic_direct_stateful_2)
                layoutAction.visible()
            }
            ReportType.CASE.value -> layoutAction.invisible()
            else -> {
                tvAction.setText(R.string.report)
                ivAction.setImageResource(R.drawable.ic_add_stateful)
                layoutAction.visible()
            }
        }

        tvTitle.setText(
            when (type) {
                ReportType.SCORE.value -> R.string.autonomy
                ReportType.CASE.value -> R.string.cases
                ReportType.SYMPTOM.value -> R.string.symptoms
                ReportType.BEHAVIOR.value -> R.string.behaviors
                else -> error("unsupported type: $type")
            }
        )

        vpAdapter = TrendingViewPagerAdapter(this, supportFragmentManager)
        vpAdapter.set(
            listOf(
                TrendingFragment.newInstance(Period.WEEK, type, scope, poiId),
                TrendingFragment.newInstance(Period.MONTH, type, scope, poiId),
                TrendingFragment.newInstance(Period.YEAR, type, scope, poiId)
            )
        )
        vp.offscreenPageLimit = vpAdapter.count
        vp.adapter = vpAdapter
        tabLayout.setupWithViewPager(vp)

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        layoutAction.setSafetyOnclickListener {
            when (type) {
                ReportType.SCORE.value -> navigator.anim(RIGHT_LEFT)
                    .openChromeTab(this@TrendingContainerActivity, JUPYTER_NOTEBOOK_URL)
                ReportType.BEHAVIOR.value -> {
                    navigator.anim(RIGHT_LEFT).startActivity(BehaviorReportActivity::class.java)
                }
                ReportType.SYMPTOM.value -> {
                    navigator.anim(RIGHT_LEFT).startActivity(SymptomReportActivity::class.java)
                }
            }
        }

        customTabServiceHandler.setUrls(arrayOf(JUPYTER_NOTEBOOK_URL))
    }

    override fun onStart() {
        super.onStart()
        customTabServiceHandler.bind()
    }

    override fun onBackPressed() {
        if (vp.currentItem != Period.WEEK) {
            vp.currentItem = Period.WEEK
        } else {
            navigator.anim(RIGHT_LEFT).finishActivity()
            super.onBackPressed()
        }
    }

}