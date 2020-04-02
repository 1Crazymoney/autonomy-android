/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.bitmark.autonomy.BuildConfig
import com.bitmark.autonomy.data.source.local.LocationCache
import com.bitmark.autonomy.data.source.local.apply
import com.bitmark.autonomy.keymanagement.ApiKeyManager.Companion.API_KEY_MANAGER
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.logging.Tracer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.LatLng
import com.tbruyelle.rxpermissions2.RxPermissions
import java.util.concurrent.Executors


class LocationService(private val context: Context, private val logger: EventLogger) {

    companion object {
        private const val TAG = "LocationService"

        const val LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    }

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val geoApiContext = GeoApiContext.Builder().apiKey(API_KEY_MANAGER.googleApiKey).build()

    private val executor = Executors.newSingleThreadExecutor()

    private val handler = Handler()

    private var lastReversedLocation: Location? = null

    private val locationUpdateCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            result ?: return
            for (location in result.locations) {
                LocationCache.getInstance().apply(location)
                Log.d(TAG, "Location changed: (${location.latitude}, ${location.longitude})")
                notifyLocationChanged(location)
                if (lastReversedLocation == null || lastReversedLocation!!.distanceTo(location) >= BuildConfig.MIN_REFRESH_DISTANCE) {
                    val reverseFunc = fun(l: Location) {
                        reverseGeoCoding(l) { place ->
                            Log.d(TAG, "place changed: $place")
                            notifyPlaceChanged(place)
                            lastReversedLocation = l
                        }
                    }
                    reverseFunc(location)
                }
            }
        }
    }

    private val locationChangeListeners = mutableListOf<LocationChangedListener>()

    fun addLocationChangeListener(listener: LocationChangedListener) {
        if (locationChangeListeners.contains(listener)) return
        locationChangeListeners.add(listener)
        getLastKnownLocation { l ->
            if (l == null) return@getLastKnownLocation
            listener.onLocationChanged(l)
        }
    }

    fun removeLocationChangeListener(listener: LocationChangedListener) {
        locationChangeListeners.remove(listener)
    }

    private fun reverseGeoCoding(l: Location, callback: (String) -> Unit) {
        executor.execute {
            try {
                val latLng = LatLng(l.latitude, l.longitude)
                val results = GeocodingApi.newRequest(geoApiContext).latlng(latLng).await()
                val place = results[0].formattedAddress
                callback(place)
            } catch (e: Throwable) {
                Tracer.ERROR.log(TAG, "failed to do geocoding reverse: ${e.message}")
            }
        }
    }

    private fun notifyLocationChanged(l: Location) {
        handler.post { locationChangeListeners.forEach { listener -> listener.onLocationChanged(l) } }
    }

    private fun notifyPlaceChanged(place: String) {
        handler.post { locationChangeListeners.forEach { listener -> listener.onPlaceChanged(place) } }
    }

    @SuppressLint("CheckResult")
    fun requestPermission(
        activity: FragmentActivity,
        grantedCallback: () -> Unit = {},
        deniedCallback: () -> Unit = {},
        permanentlyDeniedCallback: () -> Unit = {}
    ) {
        val rxPermission = RxPermissions(activity)
        rxPermission.requestEach(LOCATION_PERMISSION)
            .subscribe({ permission ->
                when {
                    permission.granted -> {
                        logger.logEvent(Event.LOCATION_PERMISSION_GRANTED)
                        grantedCallback()
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        logger.logEvent(Event.LOCATION_PERMISSION_DENIED)
                        deniedCallback()
                    }
                    else -> {
                        logger.logEvent(Event.LOCATION_PERMISSION_DENIED)
                        permanentlyDeniedCallback()
                    }
                }
            }, {})
    }

    fun isPermissionGranted(activity: FragmentActivity) =
        RxPermissions(activity).isGranted(LOCATION_PERMISSION)

    private fun getLastKnownLocation(callback: (Location?) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { l ->
            if (l != null) {
                LocationCache.getInstance().apply(l)
            }
            callback(l)
        }.addOnFailureListener { e ->
            Tracer.ERROR.log(TAG, "could not get last known location: ${e.message}")
        }
    }

    fun start(
        activity: FragmentActivity,
        resolvableErrorCallback: (ResolvableApiException) -> Unit
    ) {
        if (!isPermissionGranted(activity)) error("need to request location permission")
        buildLocationRequest({ req ->
            fusedLocationClient.requestLocationUpdates(
                req,
                locationUpdateCallback,
                Looper.getMainLooper()
            )
        }, resolvableErrorCallback)
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        fusedLocationClient.removeLocationUpdates(locationUpdateCallback)
    }

    private fun buildLocationRequest(
        successCallback: (LocationRequest) -> Unit,
        resolvableErrorCallback: (ResolvableApiException) -> Unit
    ) {
        val request = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 10000 // 10 seconds
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val settingClient = LocationServices.getSettingsClient(context)
        settingClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                Log.d(TAG, "location setting is satisfied")
                successCallback(request)
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    logger.logError(Event.LOCATION_SETTING_NEED_TO_BE_RESOLVED, e)
                    try {
                        resolvableErrorCallback(e)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, e.message)
                        logger.logError(Event.LOCATION_SETTING_CANNOT_BE_RESOLVED, e)
                    }
                } else {
                    Log.e(TAG, e.message)
                    logger.logError(Event.LOCATION_SETTING_CANNOT_BE_RESOLVED, e)
                }
            }
    }

    interface LocationChangedListener {

        fun onLocationChanged(l: Location)

        fun onPlaceChanged(place: String)
    }
}