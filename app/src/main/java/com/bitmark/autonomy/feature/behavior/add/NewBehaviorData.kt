/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.add

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class NewBehaviorData(var title: String = "", var description: String = "") : Parcelable