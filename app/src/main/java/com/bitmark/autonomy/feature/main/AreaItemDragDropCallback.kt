/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.util.view.touchhelper.ItemTouchHelperCallback
import com.bitmark.autonomy.util.view.touchhelper.ItemTouchListener


class AreaItemDragDropCallback(listener: ItemTouchListener) : ItemTouchHelperCallback(listener) {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlag = 0
        return makeMovementFlags(dragFlag, swipeFlag)
    }
}