/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.addresource.select

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.ext.setTextColorRes
import com.bitmark.autonomy.util.modelview.ResourceModelView
import kotlinx.android.synthetic.main.item_resource.view.*
import kotlinx.android.synthetic.main.item_simple_text_footer.view.*
import kotlinx.android.synthetic.main.item_simple_text_header.view.*
import java.util.*


class ResourceAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val HEADER = 0x00

        private const val BODY = 0x01

        private const val FOOTER = 0x02
    }

    private val items = mutableListOf<Item>()

    private var itemClickListener: ItemClickListener? = null

    private val onSelectedChangeListener: () -> Unit = {
        if (getSelectedResources().isNotEmpty()) {
            itemClickListener?.onSelected()
        } else {
            itemClickListener?.onDeselected()
        }
    }

    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    fun set(resources: List<ResourceModelView>) {
        items.clear()
        items.add(
            Item(
                HEADER,
                headerStringRes = R.string.important_resources_for_this_neighborhood
            )
        )
        items.addAll(resources.map { r ->
            Item(
                BODY,
                r,
                null,
                selected = false,
                selectable = true
            )
        })
        notifyDataSetChanged()
    }

    fun isEmpty() = items.count { i -> i.type == BODY } == 0

    fun addFooter() {
        val pos = items.size
        items.add(Item(FOOTER))
        notifyItemInserted(pos)
    }

    fun removeFooter() {
        val index = items.indexOfFirst { i -> i.type == FOOTER }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun add(resource: ResourceModelView, selected: Boolean = true, selectable: Boolean = true) {
        val pos = items.size
        items.add(pos, Item(BODY, resource, selected = selected, selectable = selectable))
        notifyItemInserted(pos)
    }

    fun isExisting(resource: ResourceModelView) =
        items.indexOfFirst { i -> i.type == BODY && (i.resource?.id == resource.id || i.resource?.name == resource.name) } != -1

    fun setSelected(resource: ResourceModelView, selected: Boolean = true, selectable: Boolean = true) {
        val index = items.indexOfFirst { i -> i.type == BODY && (i.resource?.id == resource.id || i.resource?.name == resource.name) }
        if (index != -1) {
            val item = items[index]
            item.selected = selected
            item.selectable = selectable
            notifyItemChanged(index)
        }
    }

    fun getSelectedResources() =
        items.filter { i -> i.type == BODY && i.selected!! }.map { i -> i.resource!! }

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
                    R.layout.item_resource,
                    parent,
                    false
                ),
                onSelectedChangeListener
            )
            FOOTER -> FooterVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_simple_text_footer,
                    parent,
                    false
                )
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
            is FooterVH -> holder.bind()
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

    class FooterVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind() {
            with(itemView) {
                tvFooter.setText(R.string.none)
                tvFooter.setTextColorRes(R.color.concord)
            }
        }
    }

    class BodyVH(view: View, onSelectedChangeListener: () -> Unit) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                tvResource.setOnClickListener {
                    if (!item.selectable!!) return@setOnClickListener
                    tvResource.isSelected = !tvResource.isSelected
                    item.selected = tvResource.isSelected
                    onSelectedChangeListener()
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                tvResource.text = item.resource!!.name.toLowerCase(Locale.getDefault())
                tvResource.isSelected = item.selected!!
                tvResource.isClickable = item.selectable!!
            }
        }
    }

    data class Item(
        val type: Int,
        val resource: ResourceModelView? = null,
        @StringRes val headerStringRes: Int? = null,
        var selected: Boolean? = null,
        var selectable: Boolean? = null
    )

    interface ItemClickListener {

        fun onSelected()

        fun onDeselected()

    }
}