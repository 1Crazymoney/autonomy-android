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
import kotlinx.android.synthetic.main.item_behavior_footer.view.*


class BehaviorReportRecyclerViewAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val BODY = 0x00

        private const val FOOTER = 0x01
    }

    private val items = mutableListOf<Item>()

    private var itemClickListener: ItemClickListener? = null

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        if (getCheckedBehaviors().isNotEmpty()) {
            itemClickListener?.onChecked()
        } else {
            itemClickListener?.onUnChecked()
        }
    }

    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    fun set(behaviors: List<BehaviorModelView>) {
        items.clear()
        items.addAll(behaviors.map { s -> Item(BODY, s, checked = false, checkable = true) })
        items.add(Item(FOOTER, null, null, null))
        notifyDataSetChanged()
    }

    fun add(behavior: BehaviorModelView, checked: Boolean = true, checkable: Boolean = true) {
        val pos = items.size - 1
        items.add(pos, Item(BODY, behavior, checked, checkable))
        notifyItemInserted(pos)
    }

    fun getCheckedBehaviors() =
        items.filter { i -> i.type == BODY && i.checked!! }.map { i -> i.behavior }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == BODY) {
            BodyVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_behavior,
                    parent,
                    false
                ),
                onCheckedChangeListener
            )
        } else {
            FooterVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_behavior_footer,
                    parent,
                    false
                ), itemClickListener
            )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (items[position].type == BODY) {
            (holder as BodyVH).bind(items[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    class BodyVH(view: View, onCheckedChangeListener: CompoundButton.OnCheckedChangeListener) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                cbBehavior.setOnCheckedChangeListener { view, isChecked ->
                    item.checked = isChecked
                    onCheckedChangeListener.onCheckedChanged(view, isChecked)
                }

                layoutRoot.setOnClickListener {
                    if (!item.checkable!!) return@setOnClickListener
                    cbBehavior.isChecked = !cbBehavior.isChecked
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                cbBehavior.isEnabled = item.checkable!!
                cbBehavior.isChecked = item.checked!!
                tvBehavior.text = item.behavior!!.behavior
                tvBehaviorDes.text = item.behavior.behaviorDes
            }
        }
    }

    class FooterVH(view: View, itemClickListener: ItemClickListener?) :
        RecyclerView.ViewHolder(view) {

        init {
            with(itemView) {
                layoutRootFooter.setOnClickListener {
                    itemClickListener?.onAddNew()
                }
            }
        }

    }

    data class Item(
        val type: Int,
        val behavior: BehaviorModelView?,
        var checked: Boolean? = null,
        var checkable: Boolean? = null
    )

    interface ItemClickListener {

        fun onChecked()

        fun onUnChecked()

        fun onAddNew()
    }
}