/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.rating

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.ext.removeWhen
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.modelview.ResourceRatingModelView
import com.bitmark.autonomy.util.modelview.ratingToDrawableRes
import kotlinx.android.synthetic.main.item_resource_rating.view.*
import kotlinx.android.synthetic.main.item_resource_rating_footer.view.*


class ResourceRatingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val BODY = 0x00

        private const val FOOTER = 0x01
    }

    private val items = mutableListOf<Item>()

    private var actionListener: ActionListener? = null

    fun setActionListener(listener: ActionListener) {
        this.actionListener = listener
    }

    fun set(data: List<ResourceRatingModelView>) {
        items.apply {
            clear()
            addAll(data.map { d -> Item(BODY, d) })
            add(Item(FOOTER))
        }
        notifyDataSetChanged()
    }

    fun addToLast(data: List<ResourceRatingModelView>, unique: Boolean = false) {
        add(data, itemCount - 1, unique)
    }

    fun addToTop(data: List<ResourceRatingModelView>, unique: Boolean = false) {
        add(data, 0, unique)
    }

    private fun add(data: List<ResourceRatingModelView>, pos: Int, unique: Boolean) {
        if (isEmpty()) {
            set(data)
        } else {
            val clone = data.toMutableList()
            if (unique) {
                clone.removeWhen { d -> items.firstOrNull { it.type == BODY && it.data!!.resource.id == d.resource.id } != null }
            }
            items.addAll(pos, clone.map { d -> Item(BODY, d) })
            notifyItemRangeInserted(pos, clone.size)
        }
    }

    fun markHighlight(id: String) {
        val pos = items.indexOfFirst { i -> i.data != null && i.data.resource.id == id }
        if (pos != -1) {
            items[pos].highlight = true
            notifyItemChanged(pos)
        }
    }

    fun isEmpty() = items.count { i -> i.type == BODY } == 0

    fun getResourceRatings() = items.filter { i -> i.type == BODY }.map { i -> i.data!! }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            BODY -> {
                BodyVH(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_resource_rating,
                        parent,
                        false
                    )
                )
            }
            FOOTER -> {
                FooterVH(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_resource_rating_footer,
                        parent,
                        false
                    ), actionListener
                )
            }
            else -> error("unsupported viewType: $viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BodyVH) {
            holder.bind(items[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    class BodyVH(view: View) : RecyclerView.ViewHolder(view) {

        internal lateinit var item: Item

        init {
            with(itemView) {
                rb.setOnRatingChangeListener { _, rating, _ ->
                    item.data!!.score = rating
                    val rbBg = rating.toInt().ratingToDrawableRes()
                    rb.setFilledDrawableRes(rbBg)
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                val data = item.data!!
                tvName.text = data.resource.name
                val rbBg = data.score.toInt().ratingToDrawableRes()
                rb.setFilledDrawableRes(rbBg)
                rb.rating = item.data.score
                if (item.highlight == true) {
                    layoutRoot.setBackgroundResource(R.color.shark)
                    rb.setEmptyDrawableRes(R.drawable.bg_circle_black)
                } else {
                    layoutRoot.setBackgroundResource(R.color.trans)
                    rb.setEmptyDrawableRes(R.drawable.bg_circle_mine_shaft)
                }
            }
        }
    }

    class FooterVH(view: View, listener: ActionListener?) : RecyclerView.ViewHolder(view) {
        init {
            with(itemView) {
                tvAdd.setSafetyOnclickListener {
                    listener?.onAddResourceClicked()
                }

                ivAdd.setSafetyOnclickListener {
                    listener?.onAddResourceClicked()
                }
            }
        }
    }

    data class Item(
        val type: Int,
        val data: ResourceRatingModelView? = null,
        var highlight: Boolean? = null
    )

    interface ActionListener {
        fun onAddResourceClicked()
    }
}