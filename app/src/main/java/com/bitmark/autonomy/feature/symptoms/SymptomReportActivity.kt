/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.FADE_IN
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import kotlinx.android.synthetic.main.activity_symptoms_report.*
import javax.inject.Inject


class SymptomReportActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: SymptomReportViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private var blocked = false

    private val adapter = SymptomsRecyclerViewAdapter()

    override fun layoutRes(): Int = R.layout.activity_symptoms_report

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.listSymptom()
    }

    override fun initComponents() {
        super.initComponents()

        disableDone()

        adapter.setItemsCheckedChangeListener(object :
            SymptomsRecyclerViewAdapter.ItemsCheckedChangeListener {
            override fun onChecked() {
                enableDone()
            }

            override fun onUnChecked() {
                disableDone()
            }
        })

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvSymptoms.layoutManager = layoutManager
        rvSymptoms.adapter = adapter

        layoutDone.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            viewModel.reportSymptoms(adapter.getCheckedSymptoms().map { it.id })
        }
    }

    private fun enableDone() {
        layoutDone.enable()
        tvDone.enable()
        ivDone.enable()
    }

    private fun disableDone() {
        layoutDone.disable()
        tvDone.disable()
        ivDone.disable()
    }

    override fun observe() {
        super.observe()

        viewModel.listSymptomLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    adapter.set(res.data()!!)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.SYMPTOM_LISTING_ERROR, res.throwable())
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.reportSymptomLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    navigator.anim(FADE_IN).finishActivity()
                    blocked = false
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.SYMPTOM_REPORT_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_report_symptoms)
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

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}