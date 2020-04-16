/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.arealist

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.feature.areasearch.AreaSearchActivity
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.main.MainActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.view.touchhelper.ItemTouchListener
import kotlinx.android.synthetic.main.fragment_area_list.*
import javax.inject.Inject


class AreaListFragment : BaseSupportFragment() {

    companion object {

        private const val SEARCH_REQUEST_CODE = 0x1A

        private const val AREA_LIST = "area_list"

        fun newInstance(areaList: ArrayList<AreaModelView>) = AreaListFragment().apply {
            val bundle = Bundle().apply { putParcelableArrayList(AREA_LIST, areaList) }
            arguments = bundle
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var viewModel: AreaListViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private val adapter = AreaListRecyclerViewAdapter()

    private val actionListener = object : AreaListRecyclerViewAdapter.ActionListener {

        override fun onAreaDeleteClicked(id: String) {
            viewModel.delete(id)
        }

        override fun onAreaEditClicked(id: String) {
            adapter.setEditable(id, true)
            Handler().postDelayed({
                activity?.showKeyBoard()
            }, 200)
        }

        override fun onAreaClicked(id: String) {
            (activity as? MainActivity)?.showArea(id)
        }

        override fun onAddClicked() {
            navigator.anim(BOTTOM_UP)
                .startActivityForResult(AreaSearchActivity::class.java, SEARCH_REQUEST_CODE)
        }

        override fun onDone(id: String, oldAlias: String, newAlias: String) {
            activity?.hideKeyBoard()
            adapter.setEditable(id, false)
            if (oldAlias == newAlias || newAlias.isEmpty()) {
                adapter.updateAlias(id, oldAlias)
            } else {
                viewModel.rename(id, newAlias)
            }

        }

    }

    override fun viewModel(): BaseViewModel? = viewModel

    override fun layoutRes(): Int = R.layout.fragment_area_list

    override fun initComponents() {
        super.initComponents()

        val areaList = arguments?.getParcelableArrayList<AreaModelView>(AREA_LIST)?.toList()
            ?: error("missing area array")
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvAreas.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
        itemDecoration.setDrawable(context!!.getDrawable(R.drawable.bg_divider)!!)
        rvAreas.addItemDecoration(itemDecoration)
        adapter.set(areaList)

        val rvTouchListener = object : ItemTouchListener() {
            override fun onMove(oldPos: Int, newPos: Int) {
                adapter.move(oldPos, newPos)
            }

            override fun onDrop(oldPos: Int, newPos: Int) {
                super.onDrop(oldPos, newPos)
                (activity as? MainActivity)?.moveArea(oldPos + 1, newPos + 1)
                viewModel.reorder(adapter.listId())
            }
        }
        val itemTouchHelper = ItemTouchHelper(AreaItemDragDropCallback(rvTouchListener))
        itemTouchHelper.attachToRecyclerView(rvAreas)

        rvAreas.adapter = adapter

        adapter.setActionListener(actionListener)
    }

    override fun onResume() {
        super.onResume()
        viewModel.listArea()
    }

    override fun observe() {
        super.observe()

        viewModel.listAreaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    adapter.set(res.data()!!)
                }
            }
        })

        viewModel.deleteAreaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val id = res.data()!!
                    adapter.remove(id)
                    (activity as? MainActivity)?.removeArea(id)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.AREA_DELETE_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_delete_area)
                    } else {
                        dialogController.showNoInternetConnection()
                    }
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.renameAreaLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data()!!
                    adapter.updateAlias(data.first, data.second)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.AREA_RENAME_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_rename_area)
                    } else {
                        dialogController.showNoInternetConnection()
                    }
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == SEARCH_REQUEST_CODE) {
            val area = AreaSearchActivity.extractResultData(data!!)
            (activity as? MainActivity)?.addArea(area)
            adapter.add(area)
        }
    }
}