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
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.Granularity
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
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.formatPeriod
import com.bitmark.autonomy.util.modelview.ReportItemModelView
import com.bitmark.autonomy.util.modelview.isNotSupported
import com.bitmark.autonomy.util.view.DividerItemDecorator
import com.bitmark.autonomy.util.view.YValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.fragment_trending.*
import java.util.*
import javax.inject.Inject


class TrendingFragment : BaseSupportFragment() {

    companion object {

        private const val PERIOD = "period"

        private const val TYPE = "type"

        private const val SCOPE = "scope"

        private const val POI_ID = "poi_id"

        private const val DEFAULT_GRAPH_CHILD_COUNT = 2

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

    private lateinit var adapter: TrendingRecyclerViewAdapter

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
        val range = getPeriodRange(period, currentStartedAtSec)
        viewModel.listReportItem(
            scope,
            type,
            range.first,
            range.second,
            poiId,
            Locale.getDefault().langCountry(),
            getGranularity(period)
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
        val itemDecoration =
            DividerItemDecorator(getDrawable(context!!, R.drawable.bg_divider)!!, 1, 1)
        rv.addItemDecoration(itemDecoration)
        rv.isNestedScrollingEnabled = false
        (rv.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        val highlightEnable = !isTemporaryUnsupported()
        adapter = TrendingRecyclerViewAdapter(highlightEnable)
        rv.adapter = adapter

        if (highlightEnable) {
            adapter.setItemHighlightListener {
                val highlightItem = adapter.getHighLightItems()
                val chart = layoutGraph.getChildAt(DEFAULT_GRAPH_CHILD_COUNT) as BarChart
                highlightBarChart(context!!, chart, highlightItem)
            }
        }

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
        tvTime.text = DateTimeUtil.formatPeriod(
            period,
            periodStartedAtMillis,
            DateTimeUtil.getDefaultTimezoneId()
        )
    }

    private fun nextPeriod() {
        periodGap++
        currentStartedAtSec =
            getStartOfPeriodSec(period, currentStartedAtSec, 1)
        showPeriod(period, currentStartedAtSec)
        val range = getPeriodRange(period, currentStartedAtSec)
        viewModel.listReportItem(
            scope,
            type,
            range.first,
            range.second,
            poiId,
            Locale.getDefault().langCountry(),
            getGranularity(period)
        )
    }

    private fun prevPeriod() {
        periodGap--
        currentStartedAtSec =
            getStartOfPeriodSec(period, currentStartedAtSec, -1)
        showPeriod(period, currentStartedAtSec)
        val range = getPeriodRange(period, currentStartedAtSec)
        viewModel.listReportItem(
            scope,
            type,
            range.first,
            range.second,
            poiId,
            Locale.getDefault().langCountry(),
            getGranularity(period)
        )
    }

    private fun getGranularity(period: Int) =
        if (period == Period.WEEK || period == Period.MONTH) Granularity.DAY.value else Granularity.MONTH.value

    private fun getPeriodRange(period: Int, startedAtSec: Long): Pair<String, String> {
        val timezone = DateTimeUtil.getDefaultTimezoneId()
        val startedAtMillis = startedAtSec * 1000
        val startedAt = DateTimeUtil.millisToString(
            startedAtMillis,
            DateTimeUtil.ISO8601_SIMPLE_FORMAT_2,
            timezone,
            timezone,
            true
        )
        val endedAtMillis = when (period) {
            Period.WEEK -> DateTimeUtil.getEndOfWeekMillis(startedAtMillis, timezone)
            Period.MONTH -> DateTimeUtil.getEndOfMonthMillis(startedAtMillis, timezone)
            Period.YEAR -> DateTimeUtil.getEndOfYearMillis(startedAtMillis, timezone)
            else -> error("unsupported now")
        }

        val endedAt = DateTimeUtil.millisToString(
            endedAtMillis,
            DateTimeUtil.ISO8601_SIMPLE_FORMAT_2,
            timezone,
            timezone,
            true
        )

        return Pair(startedAt, endedAt)
    }

    override fun observe() {
        super.observe()

        viewModel.listReportItemLiveData.asLiveData()
            .observe(this, androidx.lifecycle.Observer { res ->
                when {
                    res.isSuccess() -> {
                        progressBar.gone()

                        if (isTemporaryUnsupported()) {
                            tvGraphNotice.setText(R.string.graph_coming_soon)
                            tvGraphNotice.visible()
                            tvGraphName.gone()
                            adapter.clear()
                        } else {

                            val data = res.data()!!
                            val hasAllZero =
                                data.filter { it.value != null }.sumBy { it.value!!.toInt() } == 0

                            if (data.isEmpty()) {
                                tvNotice.visible()
                                rv.gone()
                                adapter.clear()
                                makeEmptyChart()
                                if (type == ReportType.SYMPTOM.value) {
                                    tvNotice.setText(R.string.you_did_not_report_symptoms)
                                } else if (type == ReportType.BEHAVIOR.value) {
                                    tvNotice.setText(R.string.you_did_not_report_behaviors)
                                }
                            } else if (hasAllZero) {
                                tvNotice.gone()
                                rv.visible()
                                makeEmptyChart()
                                adapter.set(data)
                            } else {
                                tvNotice.gone()
                                rv.visible()
                                val timezone = DateTimeUtil.getDefaultTimezoneId()
                                if (data.any { s ->
                                        s.startedAt != DateTimeUtil.millisToString(
                                            currentStartedAtSec * 1000,
                                            DateTimeUtil.ISO8601_SIMPLE_FORMAT_2,
                                            timezone,
                                            timezone,
                                            true
                                        )
                                    }) return@Observer
                                val isNotSupported = data.any { s -> s.isNotSupported() }

                                if (isNotSupported) {
                                    tvGraphNotice.setText(R.string.sorry_we_currently_dont_have_active_cases)
                                    tvGraphNotice.visible()
                                    tvGraphName.gone()
                                } else {
                                    tvGraphNotice.gone()
                                    tvGraphName.visible()
                                    tvGraphName.setText(if (type == ReportType.SYMPTOM.value) R.string.symptoms else R.string.behaviors)
                                    addChart(context!!, data, period)
                                }

                                adapter.set(data)
                            }
                        }
                    }

                    res.isError() -> {
                        progressBar.gone()
                        adapter.clear()
                        makeEmptyChart()
                        logger.logError(Event.TRENDING_LOADING_ERROR, res.throwable())
                    }

                    res.isLoading() -> {
                        progressBar.visible()
                    }
                }
            })
    }

    private fun isTemporaryUnsupported() = scope in listOf(
        ReportScope.POI.value,
        ReportScope.NEIGHBORHOOD.value
    ) || type in listOf(
        ReportType.SCORE.value,
        ReportType.CASE.value
    )

    private fun getStartOfPeriodSec(period: Int): Long {
        val timezone = DateTimeUtil.getDefaultTimezoneId()
        return when (period) {
            Period.WEEK -> DateTimeUtil.getStartOfThisWeekMillis(timezone)
            Period.MONTH -> DateTimeUtil.getStartOfThisMonthMillis(timezone)
            Period.YEAR -> DateTimeUtil.getStartOfThisYearMillis(timezone)
            else -> error("unsupported period")
        } / 1000
    }

    private fun getStartOfPeriodSec(period: Int, sec: Long, gap: Int): Long {
        val millis = sec * 1000
        val timezone = DateTimeUtil.getDefaultTimezoneId()
        return when (period) {
            Period.WEEK -> DateTimeUtil.getStartOfWeekMillis(millis, gap, timezone)
            Period.MONTH -> DateTimeUtil.getStartOfMonthMillis(millis, gap, timezone)
            Period.YEAR -> DateTimeUtil.getStartOfYearMillis(millis, gap, timezone)
            else -> error("unsupported period")
        } / 1000
    }

    private fun addChart(
        context: Context,
        reportItems: List<ReportItemModelView>,
        period: Int
    ) {
        val barXValues = getBarXValues(context, period)
        val barData = buildBarChartData(reportItems, barXValues, period)
        if (layoutGraph.childCount > DEFAULT_GRAPH_CHILD_COUNT) {
            val chart = layoutGraph.getChildAt(DEFAULT_GRAPH_CHILD_COUNT) as BarChart
            chart.data = barData
            chart.invalidate()
            chart.animateY(200)
        } else {
            val chartView = buildBarChart(barXValues)
            chartView.data = barData
            val params = getGraphLayoutParams(context)
            chartView.layoutParams = params
            layoutGraph.addView(chartView)
        }
    }


    private fun makeEmptyChart() {
        val xValues = getBarXValues(context!!, period)
        val barData = buildEmptyBarChartData(xValues)
        val chart = if (layoutGraph.childCount == DEFAULT_GRAPH_CHILD_COUNT) {
            buildBarChart(xValues)
        } else {
            layoutGraph.getChildAt(DEFAULT_GRAPH_CHILD_COUNT) as BarChart
        }
        chart.data = barData
        chart.invalidate()
    }

    private fun getBarXValues(context: Context, period: Int) = when (period) {
        Period.WEEK -> context.resources.getStringArray(R.array.day_of_week).toList()
        Period.MONTH -> DateTimeUtil.listDoM().map { if (it == 1 || it % 7 == 0) it.toString() else "" }
        Period.YEAR -> context.resources.getStringArray(R.array.month_of_year).toList()
        else -> error("unsupported period: $period")
    }

    private fun highlightBarChart(
        context: Context,
        chart: BarChart,
        highlightItem: Map<String, String>
    ) {
        val dataSet = chart.data.dataSets[0] as BarDataSet
        val entryCount = dataSet.entryCount
        val entries = (0 until entryCount).map { i ->
            dataSet.getEntriesForXValue(i.toFloat()).first()
                ?: error("bar entry for index $i is null")
        }
        val colors = dataSet.colors.toIntArray()
        val defaultColor = context.getColorInt(TrendingRecyclerViewAdapter.DEFAULT_COLOR)
        for (i in colors.indices) {
            if (colors[i] == Color.TRANSPARENT || colors[i] == Color.WHITE) continue
            colors[i] = defaultColor
        }
        val valueMap =
            entries.firstOrNull { it.data != null }?.data as? Map<String, IntRange> ?: return
        highlightItem.forEach { i ->
            val id = i.key
            val range = valueMap[id] ?: return@forEach
            val color = context.getColorInt(i.value)
            val c = IntArray(range.count()) { color }
            System.arraycopy(c, 0, colors, range.first, c.size)
        }
        dataSet.colors = colors.toList()
        chart.invalidate()
    }

    private fun buildEmptyBarChartData(barXValues: List<String>): BarData {
        val entries =
            barXValues.mapIndexed { index, _ ->
                val yVals = FloatArray(6) { 1f }
                yVals[0] = 6 / 100f
                BarEntry(index.toFloat(), yVals)
            }
        val dataSet = BarDataSet(entries, "")
        val colors = IntArray(6) { Color.TRANSPARENT }
        colors[0] = Color.WHITE
        dataSet.colors = colors.toList()
        dataSet.setDrawValues(false)
        return BarData(dataSet)
    }

    private fun buildBarChartData(
        reportItems: List<ReportItemModelView>,
        barXValues: List<String>,
        period: Int
    ): BarData {
        // map with key is date string and value is map of id and quantity
        val filteredDistributionMap = mutableMapOf<String, MutableMap<String, Int>>().apply {
            reportItems.forEach { item ->
                for (e in item.distribution.entries) {
                    if (containsKey(e.key)) {
                        val v = this[e.key] ?: continue
                        v[item.name] = e.value
                    } else {
                        this[e.key] = mutableMapOf(item.name to e.value)
                    }
                }
            }
        }.toMap()

        val idArray =
            filteredDistributionMap.map { i1 -> i1.value.map { i2 -> i2.key } }.flatten().distinct()

        // calculate max value of each id
        val maxValueForId = mutableMapOf<String, Int>()
        reportItems.filter { it.value != null && it.value > 0f }.forEach { i ->
            maxValueForId[i.name] = i.value!!.toInt()
        }

        // calculate additional count for display value can be divide by 5
        val maxValue = filteredDistributionMap.maxBy { i1 ->
            i1.value.map { i2 -> i2.value }.sum()
        }!!.value.map { it.value }.sum()
        var additionalCount = 0
        val mod = maxValue % 5
        if (mod != 0) {
            val tmpValue = 5 * ((maxValue + 5) / 5)
            additionalCount = tmpValue - maxValue
        }

        // for the top indicator
        additionalCount++

        // calculate total value for whole day/month
        val totalValue =
            filteredDistributionMap.map { i1 -> i1.value.map { i2 -> i2.value } }.sumBy { it.sum() } + additionalCount

        val topIndicatorVal = totalValue / 100f

        // init bar entries
        val entries = barXValues.indices.map { index ->
            val yVals = FloatArray(totalValue) { 0f }
            if (additionalCount > 0) {
                val additionalPos = totalValue - additionalCount

                // keep space for top indicator to assign later
                for (i in additionalPos + 1 until totalValue) {
                    yVals[i] = 1f
                }
            }
            BarEntry(
                index.toFloat(),
                yVals
            )
        }.toMutableList()

        // init color array with default color
        val colors = IntArray(totalValue) {
            context!!.getColorInt(TrendingRecyclerViewAdapter.DEFAULT_COLOR)
        }

        if (additionalCount > 0) {
            val additionalPos = totalValue - additionalCount

            // keep space for top indicator to assign later
            for (i in additionalPos + 1 until totalValue) {
                colors[i] = context!!.getColorInt("trans")
            }
        }

        // key is id, value is position range
        val valueRangeMap = mutableMapOf<String, IntRange>()
        var lastItemPos: Int
        idArray.forEachIndexed { index, id ->
            val maxVal = maxValueForId[id]!!
            lastItemPos = (0 until index).sumBy { i -> maxValueForId[idArray[i]]!! }
            valueRangeMap[id] = lastItemPos until lastItemPos + maxVal
            lastItemPos += maxVal
        }

        lastItemPos = 0

        // iterate for each entry (date/month)
        for (e in filteredDistributionMap) {
            val timezone = DateTimeUtil.getDefaultTimezoneId()

            // calculate index of this date
            val index = when (period) {
                Period.WEEK -> DateTimeUtil.getDoW(e.key, DateTimeUtil.DATE_FORMAT_7, timezone) - 1
                Period.MONTH -> DateTimeUtil.getDoM(e.key, DateTimeUtil.DATE_FORMAT_7, timezone) - 1
                Period.YEAR -> DateTimeUtil.getMoY(e.key, DateTimeUtil.DATE_FORMAT_8, timezone)
                else -> error("unsupported period: $period")
            }

            val yValues = entries[index].yVals!! // y values for each day/month
            e.value.toMap().forEach { (id, count) ->

                // calculate the position of this id will be located in value array
                val idIndex = idArray.indexOf(id)
                lastItemPos = (0 until idIndex).sumBy { i -> maxValueForId[idArray[i]]!! }

                val maxVal = maxValueForId[id]!!

                val yV = FloatArray(maxVal) { 0f } // y values for underline id
                for (i in 0 until count) {
                    yV[i] = 1f
                }
                // copy to the original yValues
                System.arraycopy(yV, 0, yValues, lastItemPos, yV.size)

                // increase pos for next iteration
                lastItemPos += maxVal
            }

            // assign top indicator
            val indicatorPos = totalValue - additionalCount
            yValues[indicatorPos] = topIndicatorVal
            colors[indicatorPos] = Color.WHITE

            // re-create entry
            entries[index] = BarEntry(index.toFloat(), yValues, valueRangeMap)
        }

        val dataSet = BarDataSet(entries, "")
        dataSet.setDrawValues(false)
        dataSet.colors = colors.toList()
        dataSet.barBorderColor = getColor(context!!, R.color.black)
        dataSet.barBorderWidth = 1f
        val barData = BarData(dataSet)
        barData.barWidth = 1f
        return barData
    }

    private fun buildBarChart(barXValues: List<String>) = BarChart(context).apply {
        val font = ResourcesCompat.getFont(context!!, R.font.ibm_plex_mono_light_font_family)
        val textColor = getColor(context!!, R.color.silver_2)
        description.isEnabled = false
        axisLeft.setDrawLabels(true)
        axisLeft.setDrawGridLines(false)
        axisLeft.setDrawAxisLine(false)
        axisLeft.axisMinimum = 0f
        axisLeft.textColor = textColor
        axisLeft.typeface = font
        axisLeft.textSize = 14f
        axisLeft.granularity = 1f
        axisLeft.valueFormatter = YValueFormatter()
        axisRight.setDrawLabels(false)
        axisRight.setDrawGridLines(false)
        axisRight.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textSize = 14f
        xAxis.typeface = font
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(barXValues)
        xAxis.labelCount = barXValues.size
        xAxis.isGranularityEnabled = true
        xAxis.axisLineColor = getColor(context!!, R.color.concord)
        xAxis.axisLineWidth = 1f
        xAxis.spaceMax = 0f
        xAxis.spaceMin = 0f
        xAxis.granularity = 1f
        xAxis.textColor = getColor(context!!, R.color.silver_2)
        setScaleEnabled(false)
        isDoubleTapToZoomEnabled = false
        setPinchZoom(false)
        isDragEnabled = false
        legend.isEnabled = false
        setTouchEnabled(false)
        isHighlightPerTapEnabled = false
        isHighlightPerDragEnabled = false
        setFitBars(true)
        animateY(200)

        setExtraOffsets(
            0f,
            0f,
            0f,
            context.getDimensionPixelSize(R.dimen.dp_2).toFloat()
        )
    }

    private fun getGraphLayoutParams(context: Context) = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        context.getDimensionPixelSize(R.dimen.dp_220)
    ).apply {
        addRule(RelativeLayout.BELOW, R.id.tvGraphName)
    }

}