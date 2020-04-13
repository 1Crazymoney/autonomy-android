/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.modelview.BehaviorModelView
import kotlinx.android.synthetic.main.item_behavior.view.*


class BehaviorReportRecyclerViewAdapter :
    RecyclerView.Adapter<BehaviorReportRecyclerViewAdapter.ViewHolder>() {

    private val items = mutableListOf<Item>()

    private var itemsCheckedChangeListener: ItemsCheckedChangeListener? = null

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        if (getCheckedBehaviors().isNotEmpty()) {
            itemsCheckedChangeListener?.onChecked()
        } else {
            itemsCheckedChangeListener?.onUnChecked()
        }
    }

    fun setItemsCheckedChangeListener(listener: ItemsCheckedChangeListener) {
        this.itemsCheckedChangeListener = listener
    }

    fun set(behaviors: List<BehaviorModelView>) {
        items.clear()
        items.addAll(behaviors.map { s -> Item(s) })
        notifyDataSetChanged()
    }

    fun getCheckedBehaviors() = items.filter { i -> i.checked }.map { i -> i.behavior }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_behavior,
                parent,
                false
            ),
            onCheckedChangeListener
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(view: View, onCheckedChangeListener: CompoundButton.OnCheckedChangeListener) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                cbBehavior.setOnCheckedChangeListener { view, isChecked ->
                    item.checked = isChecked
                    onCheckedChangeListener.onCheckedChanged(view, isChecked)
                }

                layoutRoot.setOnClickListener {
                    cbBehavior.isChecked = !cbBehavior.isChecked
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                tvBehavior.text = item.behavior.behavior
                tvBehaviorDes.text = item.behavior.behaviorDes
            }
        }
    }

    data class Item(val behavior: BehaviorModelView, var checked: Boolean = false)

    interface ItemsCheckedChangeListener {

        fun onChecked()

        fun onUnChecked()
    }
}