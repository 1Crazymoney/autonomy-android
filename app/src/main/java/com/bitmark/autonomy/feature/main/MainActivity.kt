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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.bitmark.autonomy.AppLifecycleHandler
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.*
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.feature.Navigator.Companion.UP_BOTTOM
import com.bitmark.autonomy.feature.arealist.AreaListFragment
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.feature.notification.NotificationId
import com.bitmark.autonomy.feature.notification.NotificationPayloadType
import com.bitmark.autonomy.feature.notification.NotificationReceivedHandler
import com.bitmark.autonomy.feature.profile.ProfileActivity
import com.bitmark.autonomy.feature.survey.SurveyContainerActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.Constants.MAX_AREA
import com.bitmark.autonomy.util.ext.hideKeyBoard
import com.bitmark.autonomy.util.ext.openAppSetting
import com.bitmark.autonomy.util.ext.openIntercom
import com.bitmark.autonomy.util.ext.unexpectedAlert
import com.bitmark.autonomy.util.modelview.AreaModelView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseAppCompatActivity() {

    companion object {

        private const val LOCATION_SETTING_CODE = 0xAE

        private val SURVEY_INTERVAL = TimeUnit.MINUTES.toMillis(10)

        private const val NOTIFICATION_BUNDLE = "notification_bundle"

        private const val NOTIFICATION_ACTION_DELAY = 500L

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

    private lateinit var adapter: MainViewPagerAdapter

    private val handler = Handler()

    private var lastSurveyTimestamp = -1L

    private var notificationBundle: Bundle? = null

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
            navigator.anim(BOTTOM_UP).startActivity(SurveyContainerActivity::class.java)
            lastSurveyTimestamp = System.currentTimeMillis()
        }, NOTIFICATION_ACTION_DELAY)
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

        notificationBundle = intent?.extras?.getBundle(NOTIFICATION_BUNDLE)
        if (notificationBundle == null) {
            goToSurveyIfSatisfied()
        } else {
            handleNotification(notificationBundle!!, adapter.count > 0)
        }
        appLifecycleHandler.addAppStateChangedListener(appStateChangedListener)
        viewModel.listArea()
    }

    private fun handleNotification(notificationBundle: Bundle, dataReady: Boolean) {
        when (notificationBundle.getInt("notification_id")) {
            NotificationId.SURVEY -> goToSurveyIfSatisfied()
            NotificationId.RISK_LEVEL_CHANGED -> {
                if (!dataReady) return
                val areaId = notificationBundle.getString(NotificationPayloadType.POI_ID)
                handler.postDelayed({ showArea(areaId) }, NOTIFICATION_ACTION_DELAY)
            }
        }
        // destroy after already handled
        this.notificationBundle = null
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

        adapter = MainViewPagerAdapter(supportFragmentManager)
        vp.adapter = adapter
        vIndicator.setViewPager(vp)

        vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                // Do nothing
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // Do nothing
            }

            override fun onPageSelected(position: Int) {
                hideKeyBoard()
            }

        })

        ivMenu.setOnClickListener {
            navigator.anim(UP_BOTTOM).startActivity(ProfileActivity::class.java)
        }

    }

    override fun observe() {
        super.observe()

        viewModel.listAreaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val areaList = res.data()!!.toMutableList()
                    val fragments = mutableListOf<Fragment>()
                    fragments.add(MainFragment.newInstance(null))
                    fragments.addAll(areaList.map { a -> MainFragment.newInstance(a) })
                    fragments.add(AreaListFragment.newInstance(ArrayList(areaList)))
                    adapter.set(fragments)
                    vp.offscreenPageLimit = MAX_AREA + 2
                    vIndicator.notifyDataSetChanged()
                    if (notificationBundle != null) {
                        handleNotification(notificationBundle!!, true)
                    }
                }

                res.isError() -> {
                    logger.logError(Event.AREA_LIST_ERROR, res.throwable())
                    dialogController.unexpectedAlert { navigator.openIntercom(true) }
                }
            }
        })
    }

    fun moveArea(fromPos: Int, toPos: Int) {
        adapter.move(fromPos, toPos)
    }

    fun removeArea(id: String) {
        val index = adapter.indexOfAreaFragment(id)
        if (index != -1) {
            adapter.remove(index)
            vIndicator.notifyDataSetChanged()
        }
    }

    fun addArea(area: AreaModelView) {
        val existing = adapter.indexOfAreaFragment(area.id) != -1
        if (existing) return
        if (adapter.add(adapter.count - 1, MainFragment.newInstance(area))) {
            vIndicator.notifyDataSetChanged()
            vp.setCurrentItem(adapter.count - 1, false)
        }
    }

    fun showArea(id: String?) {
        if (id == null) {
            vp.setCurrentItem(0, false)
        } else {
            val index = adapter.indexOfAreaFragment(id)
            if (index > 0) {
                vp.setCurrentItem(index, false)
            }
        }
    }

    fun updateAreaAlias(id: String, alias: String) {
        adapter.updateAreaAlias(id, alias)
    }

    override fun onBackPressed() {
        val currentFragment = adapter.currentFragment as? BehaviorComponent
        if ((currentFragment as? MainFragment)?.isMsa0 == true && !currentFragment.onBackPressed())
            super.onBackPressed()
        else if (currentFragment?.onBackPressed() == false) {
            vp.setCurrentItem(0, true)
        }
    }
}