/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.bitmark.autonomy.AppLifecycleHandler
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.*
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.Navigator.Companion.UP_BOTTOM
import com.bitmark.autonomy.feature.arealist.AreaListFragment
import com.bitmark.autonomy.feature.behavior.metric.BehaviorMetricActivity
import com.bitmark.autonomy.feature.debugmode.DebugModeActivity
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.feature.notification.NotificationId
import com.bitmark.autonomy.feature.notification.NotificationPayloadType
import com.bitmark.autonomy.feature.profile.ProfileActivity
import com.bitmark.autonomy.feature.splash.SplashActivity
import com.bitmark.autonomy.feature.survey.SurveyContainerActivity
import com.bitmark.autonomy.feature.symptoms.SymptomReportActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.Constants.MAX_AREA
import com.bitmark.autonomy.util.DateTimeUtil
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.AreaModelView
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseAppCompatActivity() {

    companion object {

        private const val LOCATION_SETTING_CODE = 0xAE

        private const val NOTIFICATION_BUNDLE = "notification_bundle"

        private const val AREA_LIST = "area_list"

        private const val NOTIFICATION_ACTION_DELAY = 500L

        fun getBundle(notificationBundle: Bundle? = null, areas: List<AreaModelView>? = null) =
            Bundle().apply {
                if (notificationBundle != null) {
                    putBundle(
                        NOTIFICATION_BUNDLE,
                        notificationBundle
                    )
                }
                if (areas != null) {
                    putParcelableArrayList(AREA_LIST, ArrayList(areas))
                }
            }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var locationService: LocationService

    @Inject
    internal lateinit var viewModel: MainActivityViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var appLifecycleHandler: AppLifecycleHandler

    private lateinit var adapter: MainViewPagerAdapter

    private val handler = Handler()

    private var notificationHandled = true

    private val timezoneChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.updateTimezone(DateTimeUtil.getDefaultTimezone())
        }
    }

    private val locationChangeListener = object : LocationService.LocationChangedListener {
        override fun onLocationChanged(l: Location) {
            if (appLifecycleHandler.isOnForeground()) return
            viewModel.updateLocation()
        }

        override fun onPlaceChanged(place: String) {
            // do nothing
        }

    }

    private lateinit var areaList: MutableList<AreaModelView>

    private fun goToSurveyIfSatisfied(): Boolean {
        if (!locationService.isPermissionGranted(this)) return false
        handler.postDelayed({
            navigator.anim(BOTTOM_UP).startActivity(SurveyContainerActivity::class.java)
        }, NOTIFICATION_ACTION_DELAY)
        return true
    }

    private fun goToBehaviorMetricIfSatisfied(): Boolean {
        if (!locationService.isPermissionGranted(this)) return false
        handler.postDelayed({
            navigator.anim(BOTTOM_UP).startActivity(BehaviorMetricActivity::class.java)
        }, NOTIFICATION_ACTION_DELAY)
        return true
    }

    override fun layoutRes(): Int = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onStart() {
        super.onStart()
        if (!locationService.isPermissionGranted(this)) {
            locationService.requestPermission(this, grantedCallback = {
                startLocationService()

                // re-handle notification if it has not been handled correctly due to the permission requesting
                val notificationBundle = intent?.extras?.getBundle(NOTIFICATION_BUNDLE)
                if (notificationBundle != null && !notificationHandled) {
                    notificationHandled = handleNotification(notificationBundle)
                }
            }, permanentlyDeniedCallback = {
                dialogController.alert(
                    R.string.access_to_location_required,
                    R.string.autonomy_requires_access_to_your_location
                ) {
                    navigator.openAppSetting(this)
                }
            })
        }
        viewModel.checkDebugModeEnable()
    }

    private fun startLocationService() {
        locationService.start(this) { e ->
            e.startResolutionForResult(this, LOCATION_SETTING_CODE)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val notificationBundle = intent?.extras?.getBundle(NOTIFICATION_BUNDLE)
        val directFromNotification = intent?.extras?.getBoolean("direct_from_notification") == true
        if (notificationBundle != null && directFromNotification) {
            notificationHandled = handleNotification(notificationBundle)
        }
    }

    private fun registerTimezoneChangedReceiver() {
        val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
        registerReceiver(timezoneChangedReceiver, filter)
    }

    private fun unregisterTimezoneChangedReceiver() {
        try {
            unregisterReceiver(timezoneChangedReceiver)
        } catch (ignore: Throwable) {
        }
    }

    private fun handleNotification(notificationBundle: Bundle): Boolean {
        when (val notificationId = notificationBundle.getInt("notification_id")) {
            NotificationId.SURVEY -> return goToSurveyIfSatisfied()
            NotificationId.CLEAN_AND_DISINFECT,
            NotificationId.BEHAVIOR_REPORT_ON_SELF_HIGH_RISK,
            NotificationId.BEHAVIOR_REPORT_ON_RISK_AREA -> return goToBehaviorMetricIfSatisfied()
            NotificationId.RISK_LEVEL_CHANGED -> {
                val areaId = notificationBundle.getString(NotificationPayloadType.POI_ID)
                handler.postDelayed({ showArea(areaId) }, NOTIFICATION_ACTION_DELAY)
                return true
            }
            NotificationId.ACCOUNT_SYMPTOM_FOLLOW_UP, NotificationId.ACCOUNT_SYMPTOM_SPIKE -> {
                if (!locationService.isPermissionGranted(this)) return false
                val symptoms =
                    notificationBundle.getStringArrayList(NotificationPayloadType.SYMPTOMS)
                val bundle = SymptomReportActivity.getBundle(symptoms)
                navigator.anim(RIGHT_LEFT).startActivity(SymptomReportActivity::class.java, bundle)
                return true
            }
            else -> error("unsupported notification id $notificationId")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_SETTING_CODE) {
            startLocationService()

            // re-handle notification if it has not been handled correctly due to the location setting correct
            val notificationBundle = intent?.extras?.getBundle(NOTIFICATION_BUNDLE)
            if (notificationBundle != null && !notificationHandled) {
                notificationHandled = handleNotification(notificationBundle)
            }
        }
    }

    override fun initComponents() {
        super.initComponents()

        // handle notification
        val notificationBundle = intent?.extras?.getBundle(NOTIFICATION_BUNDLE)
        if (notificationBundle != null) {
            val directFromNotification =
                intent?.extras?.getBoolean("direct_from_notification") == true
            if (directFromNotification) {
                // start app again make sure all related logic handling
                val bundle = SplashActivity.getBundle(notificationBundle)
                navigator.anim(Navigator.FADE_IN)
                    .startActivityAsRoot(SplashActivity::class.java, bundle)
                return
            } else {
                notificationHandled = handleNotification(notificationBundle)
            }
        }

        // register components
        registerTimezoneChangedReceiver()
        if (locationService.isPermissionGranted(this)) {
            startLocationService()
        }
        locationService.addLocationChangeListener(locationChangeListener)

        // init area viewpager
        adapter = MainViewPagerAdapter(supportFragmentManager)
        areaList = intent?.extras?.getParcelableArrayList<AreaModelView>(AREA_LIST)
            ?: error("missing area list")
        val fragments = mutableListOf<Fragment>()
        fragments.add(MainFragment.newInstance(null))
        fragments.addAll(areaList.map { a -> MainFragment.newInstance(a) })
        fragments.add(AreaListFragment.newInstance(ArrayList(areaList)))
        adapter.set(fragments)
        vp.offscreenPageLimit = MAX_AREA + 2
        vp.adapter = adapter
        vIndicator.setViewPager(vp)

        // set views listener
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
                adapter.closeViewSourcePanels()
            }

        })

        ivMenu.setSafetyOnclickListener {
            navigator.anim(UP_BOTTOM).startActivity(ProfileActivity::class.java)
        }

        ivDebug.setSafetyOnclickListener {
            if (!isAreaListReady()) return@setSafetyOnclickListener
            val bundle = DebugModeActivity.getBundle(areaList)
            navigator.anim(BOTTOM_UP).startActivity(DebugModeActivity::class.java, bundle)
        }

    }

    override fun deinitComponents() {
        locationService.removeLocationChangeListener(locationChangeListener)
        locationService.stop()
        unregisterTimezoneChangedReceiver()
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.checkDebugModeEnableLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    ivDebug.visibility = if (res.data()!!) View.VISIBLE else View.INVISIBLE
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "main check debug mode state error")
                }
            }
        })

        viewModel.updateTimezoneLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isError() -> {
                    logger.logError(Event.ACCOUNT_UPDATE_TIMEZONE_ERROR, res.throwable())
                }
            }
        })

        viewModel.updateLocationLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isError() -> {
                    logger.logError(Event.LOCATION_BACKGROUND_UPDATE_ERROR, res.throwable())
                }
            }
        })
    }

    private fun isAreaListReady() = ::areaList.isInitialized

    fun moveArea(fromPos: Int, toPos: Int) {
        adapter.move(fromPos, toPos)
        areaList.move(fromPos - 1, toPos - 1)
    }

    fun removeArea(id: String) {
        val fIndex = adapter.indexOfAreaFragment(id)
        if (fIndex != -1) {
            adapter.remove(fIndex)
            vIndicator.notifyDataSetChanged()
            val aIndex = areaList.indexOfFirst { a -> a.id == id }
            if (aIndex != -1) {
                areaList.removeAt(aIndex)
            }
        }
    }

    fun addArea(area: AreaModelView) {
        val existing = adapter.indexOfAreaFragment(area.id) != -1
        if (existing) return
        if (adapter.add(adapter.count - 1, MainFragment.newInstance(area))) {
            vIndicator.notifyDataSetChanged()
            vp.setCurrentItem(adapter.count - 1, false)
            areaList.add(area)
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
        areaList.find { a -> a.id == id }?.alias = alias
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