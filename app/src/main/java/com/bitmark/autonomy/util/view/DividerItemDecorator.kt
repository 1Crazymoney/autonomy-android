/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.view

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView


class DividerItemDecorator(
    private val drawable: Drawable,
    private val topRemovedItemCount: Int,
    private val bottomRemovedItemCount: Int
) : RecyclerView.ItemDecoration() {

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight

        val childCount = parent.childCount

        for (i in topRemovedItemCount..childCount - 1 - bottomRemovedItemCount) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + drawable.intrinsicHeight

            drawable.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            drawable.draw(c)
        }
    }
}