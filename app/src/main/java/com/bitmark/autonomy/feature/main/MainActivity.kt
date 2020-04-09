/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.bitmark.autonomy.AppLifecycleHandler
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.arealist.AreaListFragment
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.feature.notification.NotificationId
import com.bitmark.autonomy.feature.notification.NotificationReceivedHandler
import com.bitmark.autonomy.feature.survey.SurveyContainerActivity
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.openAppSetting
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseAppCompatActivity() {

    companion object {

        private const val LOCATION_SETTING_CODE = 0xAE

        private val SURVEY_INTERVAL = TimeUnit.MINUTES.toMillis(10)

        private const val NOTIFICATION_BUNDLE = "notification_bundle"

        fun getBundle(notificationBundle: Bundle? = null) =
            Bundle().apply {
                if (notificationBundle != null) putBundle(
                    NOTIFICATION_BUNDLE,
                    notificationBundle
                )
            }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var locationService: LocationService

    @Inject
    internal lateinit var appLifecycleHandler: AppLifecycleHandler

    @Inject
    internal lateinit var viewModel: MainActivityViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var notificationReceivedHandler: NotificationReceivedHandler

    private val handler = Handler()

    private var lastSurveyTimestamp = -1L

    private val appStateChangedListener = object : AppLifecycleHandler.AppStateChangedListener {
        override fun onForeground() {
            super.onForeground()
            goToSurveyIfSatisfied()
        }
    }

    private fun goToSurveyIfSatisfied() {
        if (!locationService.isPermissionGranted(this)
            || (lastSurveyTimestamp != -1L && System.currentTimeMillis() - lastSurveyTimestamp < SURVEY_INTERVAL)
        ) return
        handler.postDelayed({
            navigator.anim(RIGHT_LEFT).startActivity(SurveyContainerActivity::class.java)
            lastSurveyTimestamp = System.currentTimeMillis()
        }, 500)
    }

    override fun layoutRes(): Int = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = viewModel

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
    }

    private fun startLocationService() {
        locationService.start(this) { e ->
            e.startResolutionForResult(this, LOCATION_SETTING_CODE)
        }
    }

    override fun onStop() {
        locationService.stop()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationBundle = intent?.extras?.getBundle(NOTIFICATION_BUNDLE)
        if (notificationBundle != null) {
            when (notificationBundle.getInt("notification_id")) {
                NotificationId.SURVEY -> goToSurveyIfSatisfied()
            }
        } else {
            goToSurveyIfSatisfied()
        }
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

    override fun initComponents() {
        super.initComponents()

        val adapter = MainViewPagerAdapter(supportFragmentManager)
        adapter.add(MainFragment.newInstance(true))
        adapter.add(MainFragment.newInstance(false))
        adapter.add(MainFragment.newInstance(false))
        adapter.add(MainFragment.newInstance(false))
        adapter.add(AreaListFragment.newInstance())
        vp.offscreenPageLimit = 5
        vp.adapter = adapter
        vIndicator.setViewPager(vp)
    }

}