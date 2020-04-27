/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.modelview.AreaProfileModelView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.tvScore
import kotlinx.android.synthetic.main.layout_area_info.*
import kotlinx.android.synthetic.main.layout_view_source.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt


class MainFragment : BaseSupportFragment() {

    companion object {

        private const val AREA_DATA = "area_data"

        private const val AREA_PROFILE = "area_profile"

        private const val MSA_0 = "MSA_0"

        fun newInstance(areaData: AreaModelView? = null) = MainFragment().apply {
            val bundle = Bundle().apply { if (areaData != null) putParcelable(AREA_DATA, areaData) }
            arguments = bundle
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var viewModel: MainFragmentViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var locationService: LocationService

    private lateinit var areaProfile: AreaProfileModelView

    private val locationChangedListener = object : LocationService.LocationChangedListener {

        override fun onPlaceChanged(place: String) {
            if (!isMsa0 || place.isEmpty()) return
            tvLocation.text = place
        }

        override fun onLocationChanged(l: Location) {
            if (!this@MainFragment::areaProfile.isInitialized && isMsa0) {
                viewModel.getCurrentAreaProfile()
            }
        }
    }

    private var areaData: AreaModelView? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    var isMsa0 = false
        private set

    fun getAreaId() = areaData?.id

    fun updateAlias(alias: String) {
        areaData?.alias = alias
        tvLocation.text = alias
    }

    override fun layoutRes(): Int = R.layout.fragment_main

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        areaData = arguments?.getParcelable(AREA_DATA)
        isMsa0 = areaData == null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(AREA_DATA)) {
                areaData = savedInstanceState.getParcelable(AREA_DATA)
            }
            if (savedInstanceState.containsKey(AREA_PROFILE)) {
                areaProfile = savedInstanceState.getParcelable(AREA_PROFILE)!!
            }
            if (savedInstanceState.containsKey(MSA_0)) {
                isMsa0 = savedInstanceState.getBoolean(MSA_0)
            }
        }
        super.onViewCreated(view, savedInstanceState)
        locationService.addLocationChangeListener(locationChangedListener)
    }

    override fun onDestroyView() {
        locationService.removeLocationChangeListener(locationChangedListener)
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (areaData != null) outState.putParcelable(AREA_DATA, areaData)
        if (::areaProfile.isInitialized) outState.putParcelable(AREA_PROFILE, areaProfile)
        outState.putBoolean(MSA_0, isMsa0)
    }

    override fun onResume() {
        super.onResume()
        if (isMsa0) {
            viewModel.getCurrentAreaProfile()
        } else {
            viewModel.getAreaProfile(areaData!!.id)
        }
    }

    override fun initComponents() {
        super.initComponents()

        if (areaData != null) {
            tvLocation.text = areaData!!.alias
        }

        if (::areaProfile.isInitialized) {
            showData(areaProfile)
        }

        bottomSheetBehavior = BottomSheetBehavior.from(layoutViewSourceRoot)
        ivScore.setOnClickListener {
            bottomSheetBehavior.state =
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    BottomSheetBehavior.STATE_HIDDEN
                } else {
                    BottomSheetBehavior.STATE_EXPANDED
                }
        }

        rootView!!.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rootView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)

                bottomSheetBehavior.setBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {

                    val context = this@MainFragment.context!!
                    val bottomSheetHeight = context.getDimensionPixelSize(R.dimen.dp_400)
                    val triangleVerticalPadding = 2 * context.getDimensionPixelSize(R.dimen.dp_32)

                    val triangleYOffset = ivScore.y
                    val tvScoreYOffset = tvScore.y
                    val tvAppNameXOffset = tvAppName.x
                    val triangleHeight = ivScore.height
                    val tvScoreHeight = tvScore.height
                    val scaledTriangleHeight =
                        rootView!!.height - bottomSheetHeight - tvLocation.height - triangleVerticalPadding
                    val minScale = scaledTriangleHeight.toFloat() / triangleHeight
                    val scaleDelta = 1f - minScale
                    val scaledTriangleYOffset = tvLocation.y + triangleVerticalPadding / 2
                    val scaledTriangleDeltaHeight = triangleHeight * scaleDelta
                    val maxTriangleTranslateY =
                        triangleYOffset - scaledTriangleYOffset + scaledTriangleDeltaHeight / 2
                    val maxTvScoreTranslateY =
                        maxTriangleTranslateY + tvScoreHeight * scaleDelta / 2 - context.getDimensionPixelSize(
                            R.dimen.dp_6
                        ) // estimated px to make the tvScore is vertically center
                    val maxTvAppNameTranslateX = context.screenWidth / 2 + tvAppName.width / 2
                    var lastOffset = 0f

                    override fun onSlide(p0: View, offset: Float) {
                        if (offset == 0f) {
                            ivScore.scaleX = 1f
                            ivScore.scaleY = 1f
                            tvScore.scaleX = 1f
                            tvScore.scaleY = 1f
                            tvAppName.alpha = 1f
                            ivScore.y = triangleYOffset
                            tvScore.y = tvScoreYOffset
                            tvAppName.x = tvAppNameXOffset
                        } else {
                            // calculate scale
                            val scaleRate = 1f - (scaleDelta * offset)
                            ivScore.scaleX = scaleRate
                            ivScore.scaleY = scaleRate
                            tvScore.scaleX = scaleRate
                            tvScore.scaleY = scaleRate

                            // calculate alpha
                            tvAppName.alpha = 1 - offset

                            // calculate translate X for tvAppName
                            val translateXDelta = (lastOffset - offset) * maxTvAppNameTranslateX
                            tvAppName.x -= translateXDelta
                            Log.d("onSlide", "translateXDelta = $translateXDelta")

                            // calculate translate Y
                            val triangleDeltaY = (lastOffset - offset) * maxTriangleTranslateY
                            val tvScoreDeltaY = (lastOffset - offset) * maxTvScoreTranslateY
                            Log.d(
                                "onSlide",
                                "triangleDeltaY = $triangleDeltaY, tvScoreDeltaY = $tvScoreDeltaY,  offset=$offset"
                            )
                            ivScore.y += triangleDeltaY
                            tvScore.y += tvScoreDeltaY
                        }
                        lastOffset = offset
                    }

                    override fun onStateChanged(p0: View, p1: Int) {
                        // do nothing
                    }

                })
            }

        })

        /*layoutRiskLevel.setSafetyOnclickListener {
            it?.flip(layoutAreaInfo, 400)
        }*/

        /*layoutAreaInfo.setSafetyOnclickListener {
            it?.flip(layoutRiskLevel, 400)
        }*/

    }

    override fun observe() {
        super.observe()

        viewModel.getAreaProfileLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    areaProfile = res.data()!!
                    showData(areaProfile)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.AREA_PROFILE_GETTING_ERROR, res.throwable())
                }

                res.isLoading() -> {
                    if (this@MainFragment::areaProfile.isInitialized) return@Observer
                    progressBar.visible()
                }
            }
        })
    }

    private fun showData(profile: AreaProfileModelView) {
        val score = profile.score.roundToInt()
        tvScore.text = score.toString()
        ivScore.setImageResource("triangle_%03d".format(score))
        /*when {
            score < 34 -> {
                tvRiskLevel.setText(R.string.high_risk)
            }
            score < 67 -> {
                tvRiskLevel.setText(R.string.moderate_risk)
            }
            else -> {
                tvRiskLevel.setText(R.string.low_risk)
            }
        }*/

        tvConfirmedCases.text = profile.confirmed.decimalFormat()
        tvConfirmedCasesChange.text = abs(profile.confirmedDelta).decimalFormat()
        if (profile.confirmed == 0) {
            tvConfirmedCasesChange.gone()
            ivConfirmedCasesChange.gone()
        } else {
            tvConfirmedCasesChange.visible()
            ivConfirmedCasesChange.visible()
        }

        ivConfirmedCasesChange.setImageResource(if (profile.confirmedDelta > 0) R.drawable.ic_up_red else R.drawable.ic_down_green)

        tvReportedSymptom.text = profile.symptoms.decimalFormat()
        tvReportedSymptomChange.text = abs(profile.symptomsDelta).decimalFormat()
        if (profile.symptoms == 0) {
            tvReportedSymptomChange.gone()
            ivReportedSymptomChange.gone()
        } else {
            tvReportedSymptomChange.visible()
            ivReportedSymptomChange.visible()
        }

        ivReportedSymptomChange.setImageResource(if (profile.symptomsDelta > 0) R.drawable.ic_up_red else R.drawable.ic_down_green)

        tvHealthyBehavior.text = profile.behaviors.decimalFormat()
        tvHealthyBehaviorChange.text = abs(profile.behaviorsDelta).decimalFormat()
        if (profile.behaviors == 0) {
            tvHealthyBehaviorChange.gone()
            ivHealthyBehaviorChange.gone()
        } else {
            tvHealthyBehaviorChange.visible()
            ivHealthyBehaviorChange.visible()
        }

        ivHealthyBehaviorChange.setImageResource(if (profile.behaviorsDelta > 0) R.drawable.ic_up_green else R.drawable.ic_down_red)
    }

    override fun onBackPressed(): Boolean {
        return if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            true
        } else {
            super.onBackPressed()
        }
    }

}