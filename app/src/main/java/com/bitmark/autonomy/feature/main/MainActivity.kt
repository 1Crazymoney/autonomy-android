/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.AppLifecycleHandler
import com.bitmark.autonomy.BuildConfig
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.feature.respondhelp.RespondHelpActivity
import com.bitmark.autonomy.feature.survey.SurveyContainerActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.gone
import com.bitmark.autonomy.util.ext.openAppSetting
import com.bitmark.autonomy.util.ext.visible
import com.bitmark.autonomy.util.modelview.HelpRequestModelView
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseAppCompatActivity() {

    companion object {
        private const val LOCATION_SETTING_CODE = 0xAE
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var locationService: LocationService

    @Inject
    internal lateinit var appLifecycleHandler: AppLifecycleHandler

    @Inject
    internal lateinit var viewModel: MainViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    private val handler = Handler()

    private val adapter = HelpCollectionRecyclerViewAdapter()

    private var lastKnownLocation: Location? = null

    private val locationChangedListener = object : LocationService.LocationChangedListener {

        override fun onPlaceChanged(place: String) {
            tvLocation.text = place
        }

        override fun onLocationChanged(l: Location) {
            if (lastKnownLocation == null || lastKnownLocation!!.distanceTo(l) >= BuildConfig.MIN_REFRESH_DISTANCE) {
                viewModel.listHelpRequest()
            }
            lastKnownLocation = l
        }
    }

    private val appStateChangedListener = object : AppLifecycleHandler.AppStateChangedListener {
        override fun onForeground() {
            super.onForeground()
            if (!locationService.isPermissionGranted(this@MainActivity)) return
            handler.postDelayed({
                navigator.anim(RIGHT_LEFT).startActivity(SurveyContainerActivity::class.java)
            }, 200)
        }
    }

    override fun layoutRes(): Int = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        tvLocation.setText(R.string.searching)

        adapter.setItemClickListener(object : HelpCollectionRecyclerViewAdapter.ItemClickListener {
            override fun onItemClicked(item: HelpRequestModelView) {
                val bundle = RespondHelpActivity.getBundle(item)
                navigator.anim(RIGHT_LEFT).startActivity(RespondHelpActivity::class.java, bundle)
            }
        })

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvHelp.layoutManager = layoutManager
        rvHelp.adapter = adapter
    }

    override fun observe() {
        super.observe()

        viewModel.listHelpRequestLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data()!!
                    adapter.set(data)
                }

                res.isError() -> {
                    logger.logError(Event.HELP_REQUEST_LISTING_ERROR, res.throwable())
                    progressBar.gone()
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        locationService.requestPermission(this, grantedCallback = {
            startLocationService()
        }, permanentlyDeniedCallback = {
            dialogController.alert(
                R.string.access_to_location_required,
                R.string.autonomy_requires_access_to_your_location
            ) {
                navigator.openAppSetting(this)
            }
        })
        locationService.addLocationChangeListener(locationChangedListener)
        if (lastKnownLocation != null) viewModel.listHelpRequest()
    }

    private fun startLocationService() {
        locationService.start(this) { e ->
            e.startResolutionForResult(this, LOCATION_SETTING_CODE)
        }
    }

    override fun onStop() {
        locationService.removeLocationChangeListener(locationChangedListener)
        locationService.stop()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appLifecycleHandler.addAppStateChangedListener(appStateChangedListener)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        appLifecycleHandler.removeAppStateChangedListener(appStateChangedListener)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_SETTING_CODE) {
            startLocationService()
        }
    }


}