/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.requesthelp.Type
import com.bitmark.autonomy.feature.requesthelp.fromString
import com.bitmark.autonomy.feature.requesthelp.resId
import com.bitmark.autonomy.util.DateTimeUtil
import com.bitmark.autonomy.util.ext.gone
import com.bitmark.autonomy.util.ext.visible
import com.bitmark.autonomy.util.formatAgo
import com.bitmark.autonomy.util.modelview.HelpRequestModelView
import com.bitmark.autonomy.util.modelview.isResponded
import kotlinx.android.synthetic.main.item_list_help.view.*


class HelpCollectionRecyclerViewAdapter :
    RecyclerView.Adapter<HelpCollectionRecyclerViewAdapter.ViewHolder>() {

    private val items = mutableListOf<HelpRequestModelView>()

    private var itemClickListener: ItemClickListener? = null

    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    fun set(items: List<HelpRequestModelView>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun isEmpty() = items.isEmpty()

    fun finItemById(id: String) = items.find { i -> i.id == id }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_list_help,
                parent,
                false
            ), itemClickListener
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(view: View, listener: ItemClickListener?) : RecyclerView.ViewHolder(view) {

        private lateinit var item: HelpRequestModelView

        init {
            with(itemView) {
                layoutRoot.setOnClickListener {
                    listener?.onItemClicked(item)
                }
            }
        }

        fun bind(item: HelpRequestModelView) {
            this.item = item
            with(itemView) {
                if (item.isResponded()) {
                    ivCheck.visible()
                } else {
                    ivCheck.gone()
                }

                tvTime.text = DateTimeUtil.formatAgo(context, item.createdAt)
                tvContent.setText(Type.fromString(item.subject).resId)
            }
        }
    }

    interface ItemClickListener {
        fun onItemClicked(item: HelpRequestModelView)
    }
}