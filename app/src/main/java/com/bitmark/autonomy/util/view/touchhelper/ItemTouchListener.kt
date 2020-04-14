/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.view.touchhelper


abstract class ItemTouchListener {
    open fun onMove(oldPos: Int, newPos: Int) {}

    open fun onSwiped(pos: Int, direction: Int) {}

    open fun onDrop(oldPos: Int, newPos: Int) {}
}