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
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.ext.setTextColorRes
import com.bitmark.autonomy.util.modelview.BehaviorModelView
import com.bitmark.autonomy.util.modelview.BehaviorType
import kotlinx.android.synthetic.main.item_behavior.view.*
import kotlinx.android.synthetic.main.item_simple_text_footer.view.*
import kotlinx.android.synthetic.main.item_simple_text_header.view.*
import java.util.*


class BehaviorReportRecyclerViewAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val HEADER = 0x00

        private const val BODY = 0x01

        private const val FOOTER = 0x02
    }

    private val items = mutableListOf<Item>()

    private var itemClickListener: ItemClickListener? = null

    private val onSelectedChangeListener: () -> Unit = {
        if (getSelectedBehaviors().isNotEmpty()) {
            itemClickListener?.onSelected()
        } else {
            itemClickListener?.onDeselected()
        }
    }

    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    fun set(behaviors: List<BehaviorModelView>) {
        items.clear()
        items.add(Item(HEADER, headerStringRes = R.string.recommended_behaviors))
        val officialBehaviors = behaviors.filter { s -> s.type == BehaviorType.OFFICIAL }
        items.addAll(officialBehaviors.map { s ->
            Item(
                BODY,
                s,
                null,
                selected = false,
                selectable = true
            )
        })
        items.add(Item(HEADER, headerStringRes = R.string.recent_behaviors_near_you))
        val neighborhoodBehaviors = behaviors.filter { s -> s.type == BehaviorType.NEIGHBORHOOD }
        items.addAll(neighborhoodBehaviors.map { s ->
            Item(
                BODY,
                s,
                null,
                selected = false,
                selectable = true
            )
        })
        notifyDataSetChanged()
    }

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

    fun hasNeighborhoodBehaviors() =
        items.indexOfFirst { i -> i.type == BODY && i.behavior!!.type == BehaviorType.NEIGHBORHOOD } != -1

    fun add(behavior: BehaviorModelView, selected: Boolean = true, selectable: Boolean = true) {
        val pos = items.size
        items.add(pos, Item(BODY, behavior, selected = selected, selectable = selectable))
        notifyItemInserted(pos)
    }

    fun isExisting(id: String) =
        items.indexOfFirst { i -> i.type == BODY && i.behavior!!.id == id } != -1

    fun setSelected(id: String, selected: Boolean = true, selectable: Boolean = true) {
        val index = items.indexOfFirst { i -> i.behavior?.id == id }
        if (index != -1) {
            val item = items[index]
            item.selected = selected
            item.selectable = selectable
            notifyItemChanged(index)
        }
    }

    fun setSelected(behaviorIds: List<String>) {
        behaviorIds.forEach { id ->
            val index = items.indexOfFirst { i -> i.type == BODY && i.behavior!!.id == id }
            if (index != -1) {
                items[index].selected = true
                notifyItemChanged(index)
            }
        }
    }

    fun getSelectedBehaviors() =
        items.filter { i -> i.type == BODY && i.selected!! }.map { i -> i.behavior }

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
                    R.layout.item_behavior,
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
                tvBehavior.setOnClickListener {
                    if (!item.selectable!!) return@setOnClickListener
                    tvBehavior.isSelected = !tvBehavior.isSelected
                    item.selected = tvBehavior.isSelected
                    onSelectedChangeListener()
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                tvBehavior.text = item.behavior!!.name.toLowerCase(Locale.getDefault())
                tvBehavior.isSelected = item.selected!!
                tvBehavior.isClickable = item.selectable!!
            }
        }
    }

    data class Item(
        val type: Int,
        val behavior: BehaviorModelView? = null,
        @StringRes val headerStringRes: Int? = null,
        var selected: Boolean? = null,
        var selectable: Boolean? = null
    )

    interface ItemClickListener {

        fun onSelected()

        fun onDeselected()

    }
}