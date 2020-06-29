/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.autonomyprofile

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.ReportScope
import com.bitmark.autonomy.data.model.ReportType
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.rating.ResourceRatingActivity
import com.bitmark.autonomy.feature.trending.TrendingContainerActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.modelview.AutonomyProfileModelView
import kotlinx.android.synthetic.main.activity_autonomy_profile.*
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt


class AutonomyProfileActivity : BaseAppCompatActivity() {

    companion object {

        private const val AREA_DATA = "area_data"

        private const val AUTONOMY_PROFILE = "area_profile"

        fun getBundle(areaData: AreaModelView? = null) =
            Bundle().apply {
                if (areaData != null) putParcelable(AREA_DATA, areaData)
            }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var viewModel: AutonomyProfileViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private lateinit var autonomyProfile: AutonomyProfileModelView

    private var areaData: AreaModelView? = null

    private val adapter = AutonomyProfileMetricAdapter()

    private fun isAutonomyProfileReady() = ::autonomyProfile.isInitialized

    override fun layoutRes(): Int = R.layout.activity_autonomy_profile

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(AREA_DATA)) {
                areaData = savedInstanceState.getParcelable(AREA_DATA)
            }
            if (savedInstanceState.containsKey(AUTONOMY_PROFILE)) {
                autonomyProfile = savedInstanceState.getParcelable(AUTONOMY_PROFILE)!!
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (hasAreaData()) outState.putParcelable(AREA_DATA, areaData)
        if (isAutonomyProfileReady()) outState.putParcelable(AUTONOMY_PROFILE, autonomyProfile)
    }

    override fun onResume() {
        super.onResume()

        if (isIndividual()) {
            viewModel.getAutonomyProfile(me = true)
        } else if (hasAreaData()) {
            viewModel.getAutonomyProfile(
                poiId = areaData!!.id,
                lang = Locale.getDefault().langCountry(),
                allResources = false
            )
        }
    }

    override fun initComponents() {
        super.initComponents()

        areaData = intent?.extras?.getParcelable(AREA_DATA)

        if (isIndividual()) {
            tvAlias.setText(R.string.you)
        } else if (hasAreaData()) {
            tvAlias.text = areaData!!.alias
        }

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvMetric.layoutManager = layoutManager
        rvMetric.isNestedScrollingEnabled = false
        rvMetric.adapter = adapter

        if (isAutonomyProfileReady()) {
            showData(autonomyProfile)
        }

        adapter.setActionListener(object : AutonomyProfileMetricAdapter.ActionListener {

            override fun onItemClick(item: AutonomyProfileMetricAdapter.Item) {
                if (item.type == AutonomyProfileMetricAdapter.BODY_RESOURCE) {
                    val bundle = ResourceRatingActivity.getBundle(
                        autonomyProfile.id!!,
                        item.resourceData!!.resource.id
                    )
                    navigator.anim(RIGHT_LEFT)
                        .startActivity(ResourceRatingActivity::class.java, bundle)
                } else {
                    val type =
                        if (item.yourData?.symptoms != null || item.neighborData?.symptoms != null) {
                            ReportType.SYMPTOM.value
                        } else if (item.yourData?.behaviors != null || item.neighborData?.behaviors != null) {
                            ReportType.BEHAVIOR.value
                        } else if (item.neighborData?.confirm != null) {
                            ReportType.CASE.value
                        } else if (item.neighborData?.score != null) {
                            ReportType.SCORE.value
                        } else {
                            error("unsupported type")
                        }

                    val scope =
                        when {
                            item.yourData != null -> ReportScope.INDIVIDUAL.value
                            item.neighborData != null -> ReportScope.NEIGHBORHOOD.value
                            else -> ReportScope.POI.value
                        }

                    val bundle = if (isIndividual()) {
                        TrendingContainerActivity.getBundle(type, scope)
                    } else {
                        TrendingContainerActivity.getBundle(type, scope, autonomyProfile.id)
                    }

                    navigator.anim(RIGHT_LEFT)
                        .startActivity(TrendingContainerActivity::class.java, bundle)
                }
            }

            override fun onFooterClick(label: String) {
                if (!isIndividual() && (!isAutonomyProfileReady() || autonomyProfile.id == null)) return
                when (label) {
                    getString(R.string.more) -> {
                        viewModel.getAutonomyProfile(
                            areaData!!.id,
                            true,
                            Locale.getDefault().langCountry()
                        )
                    }
                    getString(R.string.add_resource) -> {
                        val bundle =
                            ResourceRatingActivity.getBundle(autonomyProfile.id!!, null, true)
                        navigator.anim(RIGHT_LEFT)
                            .startActivity(ResourceRatingActivity::class.java, bundle)
                    }
                    else -> error("unsupported action")
                }
            }

        })

        ivScore.setSafetyOnclickListener {
            handleScoreClicked()
        }

        tvScore.setSafetyOnclickListener {
            handleScoreClicked()
        }

        layoutDelta.setSafetyOnclickListener {
            handleScoreClicked()
        }

        ivDirection.setSafetyOnclickListener {
            navigator.openGoogleMapDirection(this, autonomyProfile.address!!)
        }

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        layoutMonitor.setSafetyOnclickListener {
            if (autonomyProfile.owned!!) {
                viewModel.removeArea(autonomyProfile.id!!)
            } else {
                viewModel.addArea(autonomyProfile.id!!)
            }
        }

    }

    private fun isIndividual() = areaData == null

    private fun hasAreaData() = areaData != null

    private fun handleScoreClicked() {
        if (!isIndividual() && (!isAutonomyProfileReady() || autonomyProfile.id == null)) return
        val bundle = if (isIndividual()) {
            TrendingContainerActivity.getBundle(
                ReportType.SCORE.value,
                ReportScope.INDIVIDUAL.value
            )
        } else {
            TrendingContainerActivity.getBundle(
                ReportType.SCORE.value,
                ReportScope.POI.value,
                autonomyProfile.id
            )
        }
        navigator.anim(RIGHT_LEFT).startActivity(TrendingContainerActivity::class.java, bundle)
    }

    override fun observe() {
        super.observe()

        viewModel.getAutonomyProfileLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    autonomyProfile = res.data()!!
                    showData(autonomyProfile)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.AREA_PROFILE_GETTING_ERROR, res.throwable())
                }

                res.isLoading() -> {
                    if (this@AutonomyProfileActivity::autonomyProfile.isInitialized) return@Observer
                    progressBar.visible()
                }
            }
        })

        viewModel.addAreaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    autonomyProfile.owned = true
                    setMonitorState(true)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.AREA_ADDING_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_add_area)
                    } else {
                        dialogController.showNoInternetConnection()
                    }
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.removeAreaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    autonomyProfile.owned = false
                    setMonitorState(false)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.AREA_DELETE_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_delete_area)
                    } else {
                        dialogController.showNoInternetConnection()
                    }
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })
    }

    private fun showScore(score: Int, scoreDelta: Float) {
        tvScore.text = score.toString()
        ivScore.setImageResource("triangle_%03d".format(if (score > 100) 100 else score))
        tvDelta.text = String.format("%.2f%%", abs(scoreDelta))
        when {
            scoreDelta == 0f -> {
                ivDelta.invisible()
                tvDelta.setTextColorRes(R.color.concord)
            }

            scoreDelta < 0f -> {
                ivDelta.visible()
                tvDelta.setTextColorStateList(R.color.color_persian_red_stateful)
                ivDelta.setImageResource(R.drawable.ic_down_red)
            }

            else -> {
                ivDelta.visible()
                tvDelta.setTextColorStateList(R.color.color_apple_stateful)
                ivDelta.setImageResource(R.drawable.ic_up_green)
            }
        }
    }

    private fun showData(profile: AutonomyProfileModelView) {
        if (isIndividual()) {
            layoutAddress.gone()
            layoutMonitor.gone()
        } else {
            layoutAddress.visible()
            tvAddress.text = profile.address
            layoutMonitor.visible()
            setMonitorState(profile.owned!!)
        }

        showScore(profile.autonomyScore.roundToInt(), profile.autonomyScoreDelta)
        adapter.set(profile)

    }

    private fun setMonitorState(monitored: Boolean) {
        if (monitored) {
            ivMonitor.setImageResource(R.drawable.ic_checked_stateful)
            tvMonitor.setText(R.string.monitoring)
            tvMonitor.textSize = 18f
        } else {
            ivMonitor.setImageResource(R.drawable.ic_add_stateful)
            tvMonitor.setText(R.string.monitor)
            tvMonitor.textSize = 24f
        }
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}