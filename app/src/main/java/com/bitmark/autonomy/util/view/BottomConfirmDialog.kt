/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.view

import android.content.Context
import androidx.annotation.StringRes
import com.bitmark.autonomy.R
import kotlinx.android.synthetic.main.layout_bottom_confirm_dialog.*


class BottomConfirmDialog(
    context: Context,
    @StringRes private val title: Int,
    @StringRes private val primaryMsg: Int,
    @StringRes private val secondaryMsg: Int,
    @StringRes private val positiveText: Int,
    @StringRes private val negativeText: Int,
    private val positiveClick: () -> Unit = {},
    private val negativeClick: () -> Unit = {}
) :
    BaseBottomSheetDialog(context) {

    override fun layoutRes(): Int = R.layout.layout_bottom_confirm_dialog

    override fun initComponents() {
        super.initComponents()

        tvTitle.text = context.getString(title)
        tvMsg1.text = context.getString(primaryMsg)
        tvMsg2.text = context.getString(secondaryMsg)
        tvPositive.text = context.getString(positiveText)
        tvNegative.text = context.getString(negativeText)

        tvPositive.setOnClickListener {
            positiveClick()
            dismiss()
        }

        tvNegative.setOnClickListener {
            negativeClick()
            dismiss()
        }
    }

}