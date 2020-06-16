/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.debugmode

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.Location
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.openAppSetting
import com.bitmark.autonomy.util.ext.screenWidth
import com.bitmark.autonomy.util.ext.setTextColorRes
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.modelview.DebugInfoModelView
import com.bitmark.autonomy.util.modelview.toColorRes
import com.bitmark.autonomy.util.modelview.toOpacityColorRes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_debug_mode.*
import kotlinx.android.synthetic.main.layout_debug_info_marker.view.*
import javax.inject.Inject
import kotlin.math.roundToInt


class DebugModeActivity : BaseAppCompatActivity(), OnMapReadyCallback {

    companion object {

        private const val AREA_LIST = "area_list"

        private const val LOCATION_SETTING_CODE = 0xAE

        fun getBundle(areaList: List<AreaModelView>) =
            Bundle().apply { putParcelableArrayList(AREA_LIST, ArrayList(areaList)) }
    }

    @Inject
    internal lateinit var viewModel: DebugModeViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var locationService: LocationService

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var areaList: List<AreaModelView>

    private lateinit var lastKnownLocation: Location

    private lateinit var googleMap: GoogleMap

    private var msa1Loaded = false

    private val mapComponents = mutableListOf<Pair<Circle, Marker>>()

    private val locationChangedListener = object : LocationService.LocationChangedListener {

        override fun onPlaceChanged(place: String) {
            // do nothing
        }

        override fun onLocationChanged(l: android.location.Location) {
            lastKnownLocation = Location(l.latitude, l.longitude)
            if (isMapReady()) {
                moveCamera(googleMap, lastKnownLocation)
                viewModel.getDebugInfo(lastKnownLocation)
                if (!msa1Loaded) {
                    areaList.forEach { a -> viewModel.getDebugInfo(a) }
                }
            }
        }
    }

    override fun layoutRes(): Int = R.layout.activity_debug_mode

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        areaList = intent?.extras?.getParcelableArrayList<AreaModelView>(AREA_LIST)?.toList()
            ?: error("missing area list")

        ivBack.setOnClickListener {
            navigator.anim(BOTTOM_UP).finishActivity()
        }

        locationService.addLocationChangeListener(locationChangedListener)
    }

    override fun deinitComponents() {
        locationService.removeLocationChangeListener(locationChangedListener)
        super.deinitComponents()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mv) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        if (!locationService.isPermissionGranted(this)) {
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

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map!!
        googleMap.setMinZoomPreference(10f)
        googleMap.setMaxZoomPreference(20f)
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        if (isLocationReady()) {
            moveCamera(googleMap, lastKnownLocation)
            viewModel.getDebugInfo(lastKnownLocation)
            areaList.forEach { a -> viewModel.getDebugInfo(a) }
        }
    }

    private fun moveCamera(
        googleMap: GoogleMap,
        location: Location
    ) {
        val camUpdateFactory = CameraUpdateFactory.newCameraPosition(
            CameraPosition.fromLatLngZoom(
                LatLng(
                    location.lat,
                    location.lng
                ), 14f
            )
        )

        googleMap.moveCamera(camUpdateFactory)
    }

    private fun isLocationReady() = ::lastKnownLocation.isInitialized

    private fun isMapReady() = ::googleMap.isInitialized

    override fun observe() {
        super.observe()

        viewModel.getDebugInfoLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val debugInfo = res.data()!!
                    msa1Loaded = msa1Loaded || debugInfo.areaId != null
                    drawDebugInfo(debugInfo)
                }

                res.isError() -> {
                    logger.logError(Event.DEBUG_INFO_GETTING_ERROR, res.throwable())
                }
            }
        })
    }

    private fun drawDebugInfo(debugInfo: DebugInfoModelView) {
        // add circle bound
        val tag = debugInfo.areaId ?: "msa_0"
        val existingComponentIndex =
            mapComponents.indexOfFirst { c -> c.first.tag.toString() == tag }
        if (existingComponentIndex != -1) {
            val component = mapComponents.removeAt(existingComponentIndex)
            component.first.remove()
            component.second.remove()
        }
        val latLng = LatLng(debugInfo.location.lat, debugInfo.location.lng)
        val roundedScore = debugInfo.metric.score.roundToInt()
        val strokeColorRes = toColorRes(roundedScore)
        val fillColorRes = toOpacityColorRes(roundedScore)
        val circleOptions = CircleOptions()
            .center(latLng)
            .fillColor(getColor(fillColorRes))
            .strokeColor(getColor(strokeColorRes))
            .strokeWidth(2f)
            .radius(1000.0) // 1 km
        val circle = googleMap.addCircle(circleOptions)
        circle.tag = tag

        // add insights
        val markerView = LayoutInflater.from(this).inflate(R.layout.layout_debug_info_marker, null)
        markerView.tvCoordinate.text =
            String.format("coordinate: %s", debugInfo.location.toString())
        markerView.tvMetricScore.text =
            String.format("score: %d", debugInfo.metric.score.roundToInt())
        markerView.tvMetricConfirm.text = String.format(
            "24h_confirmed_count/delta: %d/%.2f%%",
            debugInfo.metric.confirm,
            debugInfo.metric.confirmDelta
        )
        markerView.tvMetricSymptom.text = String.format(
            "24h_symptom_count/delta: %d/%.2f%%",
            debugInfo.metric.symptoms,
            debugInfo.metric.symptomsDelta
        )
        markerView.tvMetricBehavior.text = String.format(
            "24h_behavior_count/delta: %d/%.2f%%",
            debugInfo.metric.behaviors,
            debugInfo.metric.behaviorsDelta
        )
        markerView.tvUserCount.text = String.format("user_count: %d", debugInfo.users)
        markerView.tvAqi.text = String.format("aqi: %d", debugInfo.aqi)
        markerView.tvTotalSymptom.text =
            String.format("total_symptom_count: %d", debugInfo.symptoms)

        markerView.tvCoordinate.setTextColorRes(strokeColorRes)
        markerView.tvMetricScore.setTextColorRes(strokeColorRes)
        markerView.tvMetricConfirm.setTextColorRes(strokeColorRes)
        markerView.tvMetricSymptom.setTextColorRes(strokeColorRes)
        markerView.tvMetricBehavior.setTextColorRes(strokeColorRes)
        markerView.tvUserCount.setTextColorRes(strokeColorRes)
        markerView.tvAqi.setTextColorRes(strokeColorRes)
        markerView.tvTotalSymptom.setTextColorRes(strokeColorRes)

        markerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val screenWidth = screenWidth
        markerView.measure(screenWidth, screenWidth)
        markerView.layout(0, 0, screenWidth, screenWidth)
        markerView.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(
            markerView.measuredWidth,
            markerView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)
        val markerOpts = MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        val marker = googleMap.addMarker(markerOpts)
        marker.tag = tag

        mapComponents.add(Pair(circle, marker))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_SETTING_CODE) {
            startLocationService()
        }
    }

    override fun onBackPressed() {
        navigator.anim(BOTTOM_UP).finishActivity()
        super.onBackPressed()
    }
}