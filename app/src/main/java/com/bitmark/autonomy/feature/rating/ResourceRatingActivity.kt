/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.rating

import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.gone
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.ext.showNoInternetConnection
import com.bitmark.autonomy.util.ext.visible
import com.bitmark.autonomy.util.livedata.Resource
import com.bitmark.autonomy.util.view.BottomProgressDialog
import kotlinx.android.synthetic.main.activity_resource_rating.*
import javax.inject.Inject


class ResourceRatingActivity : BaseAppCompatActivity() {

    companion object {
        private const val POI_ID = "poi_id"

        fun getBundle(poiId: String) = Bundle().apply { putString(POI_ID, poiId) }
    }

    @Inject
    internal lateinit var viewModel: ResourceRatingViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private var blocked = false

    private val handler = Handler()

    private val adapter = ResourceRatingAdapter()

    override fun layoutRes(): Int = R.layout.activity_resource_rating

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val poiId = intent?.extras?.getString(POI_ID) ?: error("missing poi_id")
        viewModel.listResourceRating(poiId)
    }

    override fun initComponents() {
        super.initComponents()

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv.layoutManager = layoutManager
        rv.isNestedScrollingEnabled = false
        rv.adapter = adapter

        adapter.setActionListener(object : ResourceRatingAdapter.ActionListener {
            override fun onAddResourceClicked() {
                // TODO go to add resource
            }

        })

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        layoutSubmit.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            viewModel.updateResourceRatings(adapter.getResourceRatings())
        }
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.listResourceRatingLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    adapter.set(res.data()!!)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.RESOURCE_RATING_LISTING_ERROR, res.throwable())
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.updateResourceRatingsLiveData.asLiveData()
            .observe(this, object : Observer<Resource<Any>> {

                lateinit var progressDialog: BottomProgressDialog

                override fun onChanged(res: Resource<Any>) {
                    when {
                        res.isSuccess() -> {
                            handler.postDelayed({
                                progressDialog.dismiss()
                                navigator.anim(RIGHT_LEFT).finishActivity()
                                blocked = false
                            }, 1000)
                        }

                        res.isError() -> {
                            logger.logError(Event.RESOURCE_RATING_UPDATING_ERROR, res.throwable())
                            handler.postDelayed({
                                progressDialog.dismiss()
                                if (connectivityHandler.isConnected()) {
                                    dialogController.alert(
                                        R.string.error,
                                        R.string.could_not_submit_your_rating
                                    )
                                } else {
                                    dialogController.showNoInternetConnection()
                                }
                                blocked = false
                            }, 1000)
                        }

                        res.isLoading() -> {
                            blocked = true
                            progressDialog = BottomProgressDialog(
                                this@ResourceRatingActivity,
                                R.string.submitting,
                                R.string.report_your_ratings
                            )
                            progressDialog.show()
                        }
                    }
                }

            })
    }
}