/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.history

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.EndlessScrollListener
import com.bitmark.autonomy.util.ext.gone
import com.bitmark.autonomy.util.ext.visible
import kotlinx.android.synthetic.main.activity_symptom_history.*
import javax.inject.Inject


class SymptomHistoryActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: SymptomHistoryViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var logger: EventLogger

    private val adapter = SymptomHistoryRecyclerAdapter()

    override fun layoutRes(): Int = R.layout.activity_symptom_history

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvHistory.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        itemDecoration.setDrawable(getDrawable(R.drawable.bg_divider)!!)
        rvHistory.addItemDecoration(itemDecoration)

        val endlessScrollListener = object : EndlessScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                viewModel.nextSymptomHistory()
            }
        }

        rvHistory.addOnScrollListener(endlessScrollListener)
        rvHistory.adapter = adapter

        layoutSwipeRefresh.setColorSchemeColors(getColor(R.color.colorAccent))
        layoutSwipeRefresh.setOnRefreshListener {
            viewModel.refreshSymptomHistory()
        }

        layoutBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.nextSymptomHistory()
    }

    override fun observe() {
        super.observe()

        viewModel.nextSymptomHistoryLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    adapter.add(res.data()!!)
                }

                res.isError() -> {
                    progressBar.gone()
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.refreshSymptomHistoryLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    layoutSwipeRefresh.isRefreshing = false
                    adapter.set(res.data()!!)
                }

                res.isError() -> {
                    layoutSwipeRefresh.isRefreshing = false
                }
            }
        })
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}