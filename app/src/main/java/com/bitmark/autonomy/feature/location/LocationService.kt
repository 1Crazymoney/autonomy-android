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
import com.bitmark.autonomy.data.source.local.LocationCache
import com.bitmark.autonomy.data.source.local.apply
import com.bitmark.autonomy.keymanagement.ApiKeyManager.Companion.API_KEY_MANAGER
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.logging.Tracer
import com.bitmark.autonomy.util.isAboveQ
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.PlaceAutocompleteRequest
import com.google.maps.PlacesApi
import com.google.maps.model.LatLng
import com.tbruyelle.rxpermissions2.RxPermissions
import java.util.concurrent.Executors


class LocationService(private val context: Context, private val logger: EventLogger) {

    companion object {
        private const val TAG = "LocationService"
    }

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val geoApiContext = GeoApiContext.Builder().apiKey(API_KEY_MANAGER.googleApiKey).build()

    private val executor = Executors.newSingleThreadExecutor()

    private val handler = Handler()

    private var lastKnownPlace = ""

    private var started = false

    private var starting = false

    private val locationUpdateCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            result ?: return
            for (location in result.locations) {
                LocationCache.getInstance().apply(location)
                Log.d(TAG, "Location changed: (${location.latitude}, ${location.longitude})")
                notifyLocationChanged(location)
                val reverseFunc = fun(l: Location) {
                    reverseGeoCoding(l) { place ->
                        Log.d(TAG, "place changed: $place")
                        lastKnownPlace = place
                        notifyPlaceChanged(place)
                    }
                }
                reverseFunc(location)
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
            listener.onPlaceChanged(lastKnownPlace)
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

    fun execGeoCoding(
        placeId: String,
        success: (com.bitmark.autonomy.data.model.Location) -> Unit,
        error: (Throwable) -> Unit = {}
    ) {
        executor.execute {
            try {
                val results = GeocodingApi.newRequest(geoApiContext).place(placeId).await()
                if (results.isEmpty()) return@execute
                val r = results[0]
                handler.post {
                    success(
                        com.bitmark.autonomy.data.model.Location(
                            r.geometry.location.lat,
                            r.geometry.location.lng
                        )
                    )
                }
            } catch (e: Throwable) {
                handler.post { error(e) }
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
        val permissions = mutableListOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (isAboveQ()) permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        rxPermission.requestEach(*permissions.toTypedArray())
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
        RxPermissions(activity).isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

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
        if (started) {
            Log.d(TAG, "location request already started before")
            return
        }
        if (starting) {
            Log.d(TAG, "location request is being started, drop this request")
            return
        }

        starting = true
        buildLocationRequest({ req ->
            Log.d(TAG, "location request started")
            starting = false
            fusedLocationClient.requestLocationUpdates(
                req,
                locationUpdateCallback,
                Looper.getMainLooper()
            )
            started = true
        }, { e ->
            starting = false
            resolvableErrorCallback(e)
        })
    }

    fun stop() {
        if (!started) {
            Log.d(TAG, "location already request stopped before")
            return
        }
        handler.removeCallbacksAndMessages(null)
        fusedLocationClient.removeLocationUpdates(locationUpdateCallback)
        started = false
        starting = false
        Log.d(TAG, "location request stopped")
    }

    private fun buildLocationRequest(
        successCallback: (LocationRequest) -> Unit,
        resolvableErrorCallback: (ResolvableApiException) -> Unit
    ) {
        val request = LocationRequest.create().apply {
            interval = 20000 // 20 seconds
            fastestInterval = 20000 // 20 seconds
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            smallestDisplacement = 50f // 50 meters
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

    fun search(
        text: String,
        success: (List<PlaceAutoComplete>) -> Unit,
        error: (Throwable) -> Unit = {}
    ) {
        if (text.isEmpty()) return
        executor.execute {
            try {
                val sessionToken = PlaceAutocompleteRequest.SessionToken()
                val predictions =
                    PlacesApi.placeAutocomplete(geoApiContext, text, sessionToken).await()
                val places =
                    predictions.map { p ->
                        Tracer.DEBUG.log(TAG, "search place: ${p.description}")
                        PlaceAutoComplete(
                            p.placeId,
                            p.structuredFormatting.mainText,
                            p.structuredFormatting.secondaryText ?: p.description,
                            p.description
                        )
                    }
                handler.post { success(places) }
            } catch (e: Throwable) {
                Tracer.ERROR.log(TAG, "search place error: ${e.message}")
                handler.post { error(e) }
            }
        }

    }

    interface LocationChangedListener {

        fun onLocationChanged(l: Location)

        fun onPlaceChanged(place: String)
    }
}