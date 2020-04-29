/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.DateTimeUtil
import com.bitmark.autonomy.util.modelview.BehaviorHistoryModelView
import com.bitmark.autonomy.util.modelview.joinSymptoms
import com.bitmark.autonomy.util.modelview.timestamp
import kotlinx.android.synthetic.main.item_behavior_history.view.*


class BehaviorHistoryRecyclerAdapter :
    RecyclerView.Adapter<BehaviorHistoryRecyclerAdapter.ViewHolder>() {

    private val items = mutableListOf<BehaviorHistoryModelView>()

    fun set(items: List<BehaviorHistoryModelView>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun add(items: List<BehaviorHistoryModelView>) {
        if (items.isEmpty()) return
        val pos = items.size
        this.items.addAll(items)
        notifyItemRangeInserted(pos, items.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_behavior_history,
            parent,
            false
        )
    )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: BehaviorHistoryModelView) {
            with(itemView) {
                val date = DateTimeUtil.millisToString(
                    item.timestamp,
                    DateTimeUtil.DATE_FORMAT_2,
                    outputTimeZone = DateTimeUtil.DEFAULT_TIME_ZONE
                )
                val time = DateTimeUtil.millisToString(
                    item.timestamp,
                    DateTimeUtil.TIME_FORMAT_2,
                    outputTimeZone = DateTimeUtil.DEFAULT_TIME_ZONE
                ).toLowerCase()
                val coordinate = "(%f, %f)".format(item.location.lat, item.location.lng)
                tvPrimary.text = "%s %s".format(
                    context.getString(R.string.date_time_format).format(date, time),
                    coordinate
                )
                tvSecondary.text = item.joinSymptoms()
            }
        }
    }
}