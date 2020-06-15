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

        private const val AREA_PROFILE = "area_profile"

        private const val MSA_0 = "MSA_0"

        fun getBundle(areaData: AreaModelView? = null) =
            Bundle().apply { if (areaData != null) putParcelable(AREA_DATA, areaData) }
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

    var isMsa0 = false
        private set

    private val adapter = AutonomyProfileMetricAdapter()

    private fun isAutonomyProfileReady() = ::autonomyProfile.isInitialized

    override fun layoutRes(): Int = R.layout.activity_autonomy_profile

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(AREA_DATA)) {
                areaData = savedInstanceState.getParcelable(AREA_DATA)
            }
            if (savedInstanceState.containsKey(AREA_PROFILE)) {
                autonomyProfile = savedInstanceState.getParcelable(AREA_PROFILE)!!
            }
            if (savedInstanceState.containsKey(MSA_0)) {
                isMsa0 = savedInstanceState.getBoolean(MSA_0)
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (areaData != null) outState.putParcelable(AREA_DATA, areaData)
        if (isAutonomyProfileReady()) outState.putParcelable(AREA_PROFILE, autonomyProfile)
        outState.putBoolean(MSA_0, isMsa0)
    }

    override fun onResume() {
        super.onResume()
        if (isMsa0) {
            viewModel.getYourAutonomyProfile()
        } else {
            viewModel.getAutonomyProfile(areaData!!.id, lang = Locale.getDefault().langCountry())
        }
    }

    override fun initComponents() {
        super.initComponents()

        areaData = intent?.extras?.getParcelable(AREA_DATA)
        isMsa0 = areaData == null

        if (isMsa0) {
            tvAlias.setText(R.string.you)
        } else {
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
                val type =
                    if (item.yourData?.symptoms != null || item.neighborData?.symptoms != null) {
                        ReportType.SYMPTOM.value
                    } else if (item.yourData?.behaviors != null || item.neighborData?.behaviors != null) {
                        ReportType.BEHAVIOR.value
                    } else if (item.neighborData?.confirm != null) {
                        ReportType.CASE.value
                    } else {
                        error("invalid handling")
                    }

                val bundle = if (isMsa0) {
                    val scope =
                        if (item.yourData != null) ReportScope.INDIVIDUAL.value else ReportScope.NEIGHBORHOOD.value
                    TrendingContainerActivity.getBundle(type, scope)
                } else {
                    val scope = ReportScope.POI.value
                    TrendingContainerActivity.getBundle(type, scope, areaData!!.id)
                }

                navigator.anim(RIGHT_LEFT)
                    .startActivity(TrendingContainerActivity::class.java, bundle)
            }

            override fun onFooterClick(label: String) {
                when (label) {
                    getString(R.string.more) -> viewModel.getAutonomyProfile(
                        areaData!!.id,
                        true,
                        Locale.getDefault().langCountry()
                    )
                    getString(R.string.view_your_rating), getString(R.string.add_rating) -> {
                        val bundle = ResourceRatingActivity.getBundle(areaData!!.id)
                        navigator.anim(RIGHT_LEFT)
                            .startActivity(ResourceRatingActivity::class.java, bundle)
                    }
                    getString(R.string.add_resource) -> {
                        val bundle = ResourceRatingActivity.getBundle(areaData!!.id, true)
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

    }

    private fun handleScoreClicked() {
        val bundle = if (isMsa0) {
            TrendingContainerActivity.getBundle(
                ReportType.SCORE.value,
                ReportScope.INDIVIDUAL.value
            )
        } else {
            TrendingContainerActivity.getBundle(
                ReportType.SCORE.value,
                ReportScope.POI.value,
                areaData!!.id
            )
        }
        navigator.anim(RIGHT_LEFT).startActivity(TrendingContainerActivity::class.java, bundle)
    }

    override fun observe() {
        super.observe()

        viewModel.getAreaProfileLiveData.asLiveData().observe(this, Observer { res ->
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
        if (isMsa0) {
            layoutAddress.gone()
        } else {
            layoutAddress.visible()
            tvAddress.text = profile.address
        }

        showScore(profile.autonomyScore.roundToInt(), profile.autonomyScoreDelta)
        adapter.set(profile)

    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}