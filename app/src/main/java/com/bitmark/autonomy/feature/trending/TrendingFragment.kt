/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.trending

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.ReportScope
import com.bitmark.autonomy.data.model.ReportType
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ChromeCustomTabServiceHandler
import com.bitmark.autonomy.util.Constants.CORONA_DATA_URL
import com.bitmark.autonomy.util.Constants.JUPYTER_NOTEBOOK_URL
import com.bitmark.autonomy.util.DateTimeUtil
import com.bitmark.autonomy.util.ext.gone
import com.bitmark.autonomy.util.ext.langCountry
import com.bitmark.autonomy.util.ext.openChromeTab
import com.bitmark.autonomy.util.ext.visible
import com.bitmark.autonomy.util.formatPeriod
import com.bitmark.autonomy.util.modelview.isNotSupported
import com.bitmark.autonomy.util.view.DividerItemDecorator
import kotlinx.android.synthetic.main.fragment_trending.*
import java.util.*
import javax.inject.Inject


class TrendingFragment : BaseSupportFragment() {

    companion object {

        private const val PERIOD = "period"

        private const val TYPE = "type"

        private const val SCOPE = "scope"

        private const val POI_ID = "poi_id"

        fun newInstance(period: Int, type: String, scope: String, poiId: String? = null) =
            TrendingFragment().apply {
                val bundle = Bundle().apply {
                    putInt(PERIOD, period)
                    putString(TYPE, type)
                    putString(SCOPE, scope)
                    if (poiId != null) putString(POI_ID, poiId)
                }
                arguments = bundle
            }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: TrendingViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var customTabServiceHandler: ChromeCustomTabServiceHandler

    private lateinit var type: String

    private lateinit var scope: String

    private var period = -1

    private var poiId: String? = null

    private var currentStartedAtSec = -1L

    private var periodGap = 0

    private val adapter = TrendingRecyclerViewAdapter()

    override fun layoutRes(): Int = R.layout.fragment_trending

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        type = arguments?.getString(TYPE) ?: error("missing type")
        scope = arguments?.getString(SCOPE) ?: error("missing scope")
        period = arguments?.getInt(PERIOD) ?: error("missing period")
        poiId = arguments?.getString(POI_ID)
        if (scope == ReportScope.POI.value && poiId == null) error("missing poi_id")

        currentStartedAtSec = getStartOfPeriodSec(period)

    }

    override fun onStart() {
        super.onStart()
        customTabServiceHandler.bind()
    }

    override fun onResume() {
        super.onResume()
        viewModel.listReportItem(
            scope,
            type,
            getPeriodRangeSec(period, currentStartedAtSec),
            poiId,
            Locale.getDefault().langCountry()
        )
    }

    override fun initComponents() {
        super.initComponents()

        if (type == ReportType.SCORE.value) {
            vDivider4.visible()
            tvDesc1.visible()
            tvDesc2.visible()

            val desc = getString(R.string.the_autonomy_score_is_a_normalized_score)
            val spannableString = SpannableString(desc)
            val linkText1 = getString(R.string.corona_data_scraper_project)
            val startIndex1 = desc.indexOf(linkText1)
            if (startIndex1 != -1) {
                spannableString.setSpan(
                    UnderlineSpan(),
                    startIndex1,
                    startIndex1 + linkText1.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                spannableString.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        navigator.anim(RIGHT_LEFT).openChromeTab(context!!, CORONA_DATA_URL)
                    }
                }, startIndex1, startIndex1 + linkText1.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }

            val linkText2 = getString(R.string.autonomy_score_jupyter_notebook)
            val startIndex2 = desc.indexOf(linkText2)
            if (startIndex2 != -1) {
                spannableString.setSpan(
                    UnderlineSpan(),
                    startIndex2,
                    startIndex2 + linkText2.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                spannableString.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        navigator.anim(RIGHT_LEFT).openChromeTab(context!!, JUPYTER_NOTEBOOK_URL)
                    }
                }, startIndex2, startIndex2 + linkText2.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }

            tvDesc2.movementMethod = LinkMovementMethod()
            tvDesc2.setLinkTextColor(getColor(context!!, R.color.white))
            tvDesc2.highlightColor = Color.TRANSPARENT
            tvDesc2.text = spannableString

        } else {
            vDivider4.gone()
            tvDesc1.gone()
            tvDesc2.gone()
        }

        val layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
        rv.layoutManager = layoutManager
        val itemDecoration = DividerItemDecorator(getDrawable(context!!, R.drawable.bg_divider)!!)
        rv.addItemDecoration(itemDecoration)
        rv.adapter = adapter

        ivNext.isEnabled = currentStartedAtSec != getStartOfPeriodSec(period)
        showPeriod(period, currentStartedAtSec)

        ivNext.setOnClickListener {
            nextPeriod()
        }

        ivPrev.setOnClickListener {
            prevPeriod()
        }

        customTabServiceHandler.setUrls(arrayOf(CORONA_DATA_URL, JUPYTER_NOTEBOOK_URL))
    }

    private fun showPeriod(period: Int, periodStartedAtSec: Long) {
        ivNext.isEnabled = periodStartedAtSec != getStartOfPeriodSec(period)
        ivPrev.isEnabled = getStartOfPeriodSec(period, periodStartedAtSec, -1) >= 0L
        val periodStartedAtMillis = periodStartedAtSec * 1000
        tvTime.text = DateTimeUtil.formatPeriod(period, periodStartedAtMillis)
    }

    private fun nextPeriod() {
        periodGap++
        currentStartedAtSec =
            getStartOfPeriodSec(period, currentStartedAtSec, 1)
        showPeriod(period, currentStartedAtSec)
        viewModel.listReportItem(
            scope,
            type,
            getPeriodRangeSec(period, currentStartedAtSec),
            poiId,
            Locale.getDefault().langCountry()
        )
    }

    private fun prevPeriod() {
        periodGap--
        currentStartedAtSec =
            getStartOfPeriodSec(period, currentStartedAtSec, -1)
        showPeriod(period, currentStartedAtSec)
        viewModel.listReportItem(
            scope,
            type,
            getPeriodRangeSec(period, currentStartedAtSec),
            poiId,
            Locale.getDefault().langCountry()
        )
    }

    private fun getPeriodRangeSec(period: Int, startedAtSec: Long): LongRange {
        val startedAtMillis = startedAtSec * 1000
        return LongRange(
            startedAtSec, when (period) {
                Period.WEEK -> DateTimeUtil.getEndOfWeekMillis(startedAtMillis)
                Period.MONTH -> DateTimeUtil.getEndOfMonthMillis(startedAtMillis)
                Period.YEAR -> DateTimeUtil.getEndOfYearMillis(startedAtMillis)
                else -> error("unsupported now")
            } / 1000
        )
    }

    override fun observe() {
        super.observe()

        viewModel.listReportItemLiveData.asLiveData()
            .observe(this, androidx.lifecycle.Observer { res ->
                when {
                    res.isSuccess() -> {
                        val data = res.data()!!
                        if (data.any { s -> s.startedAt != currentStartedAtSec }) return@Observer
                        val isNotSupported = data.any { s -> s.isNotSupported() }
                        if (isNotSupported) {
                            tvGraph.setText(R.string.sorry_we_currently_dont_have_active_cases)
                        } else {
                            tvGraph.setText(R.string.graph_coming_soon)
                        }
                        adapter.set(data)
                    }

                    res.isError() -> {
                        logger.logError(Event.TRENDING_LOADING_ERROR, res.throwable())
                    }
                }
            })
    }

    private fun getStartOfPeriodSec(period: Int) = when (period) {
        Period.WEEK -> DateTimeUtil.getStartOfThisWeekMillis()
        Period.MONTH -> DateTimeUtil.getStartOfThisMonthMillis()
        Period.YEAR -> DateTimeUtil.getStartOfThisYearMillis()
        else -> error("unsupported period")
    } / 1000

    private fun getStartOfPeriodSec(period: Int, sec: Long, gap: Int): Long {
        val millis = sec * 1000
        return when (period) {
            Period.WEEK -> DateTimeUtil.getStartOfWeekMillis(millis, gap)
            Period.MONTH -> DateTimeUtil.getStartOfMonthMillis(millis, gap)
            Period.YEAR -> DateTimeUtil.getStartOfYearMillis(millis, gap)
            else -> error("unsupported period")
        } / 1000
    }
}