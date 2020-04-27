/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StyleRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


abstract class BaseBottomSheetDialog(
    context: Context,
    private val contentHeightPx: Int = -1,
    @StyleRes theme: Int = 0,
    private val callback: BottomSheetBehavior.BottomSheetCallback? = null
) :
    BottomSheetDialog(context, theme) {

    protected lateinit var view: View

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = View.inflate(
            this.context,
            R.layout.design_bottom_sheet_dialog,
            null as ViewGroup?
        ) as FrameLayout
        val coordinator = container.findViewById<View>(R.id.coordinator) as CoordinatorLayout
        view = this.layoutInflater.inflate(layoutRes(), coordinator, false)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            contentHeightPx
        )
        if (contentHeightPx == -1) {
            setContentView(view)
        } else {
            setContentView(view, params)
        }

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        initComponents()

        val bottomSheet =
            findViewById<FrameLayout>(R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from<FrameLayout>(bottomSheet)
        setOnShowListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        if (callback != null) behavior.setBottomSheetCallback(callback)

    }

    abstract fun layoutRes(): Int

    open protected fun initComponents() {}
}