/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.areasearch

import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.areasearch.AreaAutoCompleteRecyclerViewAdapter.Companion.RESOURCE_PLACE
import com.bitmark.autonomy.feature.autonomyprofile.AutonomyProfileActivity
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.feature.location.PlaceAutoComplete
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.activity_area_search.*
import java.util.*
import javax.inject.Inject


class AreaSearchActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var locationService: LocationService

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    @Inject
    internal lateinit var viewModel: AreaSearchViewModel

    private val handler = Handler()

    private var blocked = false

    private val placeAdapter = AreaAutoCompleteRecyclerViewAdapter()

    private val resourceAdapter = ResourceAdapter()

    override fun layoutRes(): Int = R.layout.activity_area_search

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.listResources(Locale.getDefault().langCountry())
    }

    override fun initComponents() {
        super.initComponents()

        val placeLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvPlaces.layoutManager = placeLayoutManager
        val itemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        itemDecoration.setDrawable(getDrawable(R.drawable.bg_divider)!!)
        rvPlaces.addItemDecoration(itemDecoration)
        rvPlaces.adapter = placeAdapter

        val resourceLayoutManager = FlexboxLayoutManager(this)
        resourceLayoutManager.flexDirection = FlexDirection.ROW
        resourceLayoutManager.justifyContent = JustifyContent.FLEX_START
        resourceLayoutManager.flexWrap = FlexWrap.WRAP
        rvResources.layoutManager = resourceLayoutManager
        rvResources.adapter = resourceAdapter

        rvPlaces.gone()
        rvResources.gone()

        placeAdapter.setItemClickListener(object :
            AreaAutoCompleteRecyclerViewAdapter.ItemClickListener {
            override fun onItemClicked(item: AreaAutoCompleteRecyclerViewAdapter.Item) {
                hideKeyBoard()
                if (blocked) return
                if (connectivityHandler.isConnected()) {
                    if (item.type == RESOURCE_PLACE) {
                        navigateToProfile(item.area!!)
                    } else {
                        val execFunc = fun(place: PlaceAutoComplete) {
                            locationService.execGeoCoding(place.placeId, { l ->
                                blocked = false
                                viewModel.createArea(place.primaryText, place.desc, l.lat, l.lng)
                            }, {
                                blocked = false
                            })
                        }
                        execFunc(item.place!!)
                        blocked = true
                    }

                } else {
                    dialogController.showNoInternetConnection()
                }

            }
        })

        resourceAdapter.setItemClickListener { item ->
            if (blocked) return@setItemClickListener
            blocked = true
            edtName.setText(item.resource!!.name)
            edtName.setSelection(edtName.text!!.length)
            viewModel.listPlace(item.resource.id!!)
        }

        edtName.doOnTextChanged { text, _, _, _ ->
            if (blocked) return@doOnTextChanged
            handler.removeCallbacksAndMessages(null)
            val searchText = text.toString()
            if (searchText.isEmpty()) {
                placeAdapter.clear()
                rvPlaces.gone()
                rvResources.visible()
                tvNotice.gone()
            } else {
                handler.postDelayed({
                    locationService.search(searchText,
                        { places ->
                            if (places.isEmpty()) {
                                placeAdapter.clear()
                            } else {
                                placeAdapter.setAutocompletePlaces(places, searchText)
                            }
                            rvPlaces.visible()
                            rvResources.gone()
                        })
                }, 500)
            }

        }

        edtName.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                navigator.anim(BOTTOM_UP).finishActivity()
                true
            }
            false
        }

        ivClear.setOnClickListener {
            edtName.setText("")
        }
    }

    private fun navigateToProfile(area: AreaModelView) {
        val bundle = AutonomyProfileActivity.getBundle(areaData = area)
        navigator.anim(RIGHT_LEFT).startActivity(AutonomyProfileActivity::class.java, bundle)
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.listResourceLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    resourceAdapter.set(res.data()!!)
                    rvResources.visible()
                    placeAdapter.clear()
                    rvPlaces.gone()
                    blocked = false
                }

                res.isError() -> {
                    logger.logError(Event.SUGGESTED_RESOURCE_LISTING_ERROR, res.throwable())
                    progressBar.gone()
                    blocked = false
                }

                res.isLoading() -> {
                    blocked = true
                    progressBar.visible()
                }
            }
        })

        viewModel.listPlaceLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data()!!
                    if (data.isEmpty()) {
                        rvResources.gone()
                        rvPlaces.gone()
                        tvNotice.visible()
                    } else {
                        placeAdapter.setResourcePlaces(res.data()!!)
                        rvResources.gone()
                        rvPlaces.visible()
                    }
                    blocked = false
                }

                res.isError() -> {
                    logger.logError(Event.AREA_AUTOCOMPLETE_ERROR, res.throwable())
                    progressBar.gone()
                    blocked = false
                }

                res.isLoading() -> {
                    blocked = true
                    progressBar.visible()
                }
            }
        })

        viewModel.createAreaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data()!!
                    navigateToProfile(data)
                    blocked = false
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.AREA_ADDING_ERROR, res.throwable())
                    blocked = false
                }

                res.isLoading() -> {
                    blocked = true
                    progressBar.visible()
                }
            }
        })
    }

    override fun onBackPressed() {
        navigator.anim(BOTTOM_UP).finishActivity()
        super.onBackPressed()
    }
}