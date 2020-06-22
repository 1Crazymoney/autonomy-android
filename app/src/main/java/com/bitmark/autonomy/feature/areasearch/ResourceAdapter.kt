/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.areasearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.modelview.ResourceModelView
import kotlinx.android.synthetic.main.item_resource_2.view.*
import kotlinx.android.synthetic.main.item_simple_text_header.view.*
import java.util.*


class ResourceAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val HEADER = 0x00

        private const val BODY = 0x01

    }

    private val items = mutableListOf<Item>()

    private var itemClickListener: ((Item) -> Unit)? = null

    fun setItemClickListener(listener: (Item) -> Unit) {
        this.itemClickListener = listener
    }

    fun set(resources: List<ResourceModelView>) {
        items.clear()
        items.add(
            Item(
                HEADER,
                headerStringRes = R.string.or_tap_to_find_resource
            )
        )
        items.addAll(resources.map { r ->
            Item(
                BODY,
                r,
                null
            )
        })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> HeaderVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_simple_text_header,
                    parent,
                    false
                )
            )
            BODY -> BodyVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_resource_2,
                    parent,
                    false
                ),
                itemClickListener
            )
            else -> error("unsupported viewType: $viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is HeaderVH -> holder.bind(item)
            is BodyVH -> holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            with(itemView) {
                tvHeader.text = context.getString(item.headerStringRes!!)
            }
        }
    }

    class BodyVH(view: View, itemClickListener: ((Item) -> Unit)?) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                tvResource.setOnClickListener {
                    itemClickListener?.invoke(item)
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                tvResource.text = item.resource!!.name.toLowerCase(Locale.getDefault())
            }
        }
    }

    data class Item(
        val type: Int,
        val resource: ResourceModelView? = null,
        @StringRes val headerStringRes: Int? = null
    )
}