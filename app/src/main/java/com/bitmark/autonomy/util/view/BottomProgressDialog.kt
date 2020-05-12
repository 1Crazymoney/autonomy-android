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
import kotlinx.android.synthetic.main.layout_bottom_progress_dialog.*


class BottomProgressDialog(context: Context, @StringRes private val titleStringRes: Int, @StringRes private val msgStringRes: Int) :
    BaseBottomSheetDialog(context) {

    override fun layoutRes(): Int = R.layout.layout_bottom_progress_dialog

    override fun initComponents() {
        super.initComponents()
        setCancelable(false)

        tvTitle.setText(titleStringRes)
        tvMsg.setText(msgStringRes)

    }
}