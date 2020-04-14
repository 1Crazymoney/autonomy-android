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
import android.view.View
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.flip
import com.bitmark.autonomy.util.ext.setImageResource
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.modelview.AreaModelView
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.layout_risk_level_des.*
import javax.inject.Inject


class MainFragment : BaseSupportFragment() {

    companion object {

        private const val AREA_DATA = "area_data"

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

    private var score = -1

    private val locationChangedListener = object : LocationService.LocationChangedListener {

        override fun onPlaceChanged(place: String) {
            if (areaData != null || place.isEmpty()) return
            tvLocation.text = place
        }

        override fun onLocationChanged(l: Location) {
            if (score == -1) {
                viewModel.getHealthScore()
            }
        }
    }

    private var areaData: AreaModelView? = null

    override fun layoutRes(): Int = R.layout.fragment_main

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        areaData = arguments?.getParcelable(AREA_DATA)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationService.addLocationChangeListener(locationChangedListener)
    }

    override fun onDestroyView() {
        locationService.removeLocationChangeListener(locationChangedListener)
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getHealthScore()
    }

    override fun initComponents() {
        super.initComponents()

        if (areaData != null) {
            tvLocation.text = areaData!!.alias
            tvScore.text = areaData!!.score.toString()
        }

        layoutRiskLevel.setSafetyOnclickListener {
            it?.flip(layoutAreaInfo, 400)
        }

        layoutAreaInfo.setSafetyOnclickListener {
            it?.flip(layoutRiskLevel, 400)
        }

    }

    override fun observe() {
        super.observe()

        viewModel.getHealthScoreLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    score = res.data()!!.toInt()
                    tvScore.text = score.toString()
                    ivScore.setImageResource("triangle_%03d".format(score))
                    when {
                        score < 34 -> {
                            tvRiskLevel.setText(R.string.high_risk)
                            tvRiskLevelDes.setText(R.string.high_risk)
                        }
                        score < 67 -> {
                            tvRiskLevel.setText(R.string.moderate_risk)
                            tvRiskLevelDes.setText(R.string.moderate_risk)
                        }
                        else -> {
                            tvRiskLevel.setText(R.string.low_risk)
                            tvRiskLevelDes.setText(R.string.low_risk)
                        }
                    }
                }

                res.isError() -> {
                    logger.logError(Event.HEALTH_SCORE_GETTING_ERROR, res.throwable())
                }
            }
        })
    }
}