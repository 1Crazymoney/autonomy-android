/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.view

import com.bitmark.autonomy.util.ext.abbreviate
import com.github.mikephil.charting.formatter.ValueFormatter


class YValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return if (value == 0f) "" else value.toInt().abbreviate()
    }
}