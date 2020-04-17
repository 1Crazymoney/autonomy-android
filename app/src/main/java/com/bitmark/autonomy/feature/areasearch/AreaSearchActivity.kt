/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.areasearch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.gone
import com.bitmark.autonomy.util.ext.showNoInternetConnection
import com.bitmark.autonomy.util.ext.visible
import com.bitmark.autonomy.util.modelview.AreaModelView
import kotlinx.android.synthetic.main.activity_area_search.*
import javax.inject.Inject


class AreaSearchActivity : BaseAppCompatActivity() {

    companion object {

        private const val AREA = "area"

        fun extractResultData(intent: Intent): AreaModelView = intent.getParcelableExtra(AREA)

    }

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

    override fun layoutRes(): Int = R.layout.activity_area_search

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = AreaAutoCompleteRecyclerViewAdapter()
        rvPlaces.layoutManager = layoutManager
        rvPlaces.adapter = adapter

        adapter.setItemClickListener(object :
            AreaAutoCompleteRecyclerViewAdapter.ItemClickListener {
            override fun onItemClicked(item: AreaAutoCompleteRecyclerViewAdapter.Item) {
                if (connectivityHandler.isConnected()) {
                    if (blocked) return
                    val execFunc = fun(item: AreaAutoCompleteRecyclerViewAdapter.Item) {
                        locationService.execGeoCoding(item.id, { l ->
                            blocked = false
                            viewModel.addArea(item.name, item.address, l)
                        }, {
                            blocked = false
                        })
                    }
                    execFunc(item)
                    blocked = true
                } else {
                    dialogController.showNoInternetConnection()
                }

            }
        })

        edtName.doOnTextChanged { text, _, _, _ ->
            if (blocked) return@doOnTextChanged
            handler.removeCallbacksAndMessages(null)
            val searchText = text.toString()
            handler.postDelayed({
                locationService.search(searchText,
                    { places ->
                        adapter.set(places, searchText)
                    })
            }, 1000)
        }

        ivExit.setOnClickListener {
            navigator.anim(BOTTOM_UP).finishActivity()
        }
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.addAreaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data()!!
                    val bundle = Bundle().apply {
                        putParcelable(AREA, data)
                    }
                    val intent = Intent().apply { putExtras(bundle) }
                    navigator.anim(BOTTOM_UP).finishActivityForResult(intent, Activity.RESULT_OK)
                    blocked = false
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.AREA_ADDING_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_add_area)
                    } else {
                        dialogController.showNoInternetConnection()
                    }
                    blocked = false
                }

                res.isLoading() -> {
                    blocked = true
                    progressBar.visible()
                }
            }
        })
    }
}