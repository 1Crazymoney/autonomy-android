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
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.modelview.ResourceRatingModelView
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

        private lateinit var item: Item

        init {
            with(itemView) {
                rb.setOnRatingChangeListener { _, rating, _ ->
                    item.data!!.score = rating.toInt()
                    val rbBg = getRatingBarBgRes(rating.toInt())
                    rb.setFilledDrawableRes(rbBg)
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                val data = item.data!!
                tvName.text = data.resource.name
                val rbBg = getRatingBarBgRes(data.score)
                rb.setFilledDrawableRes(rbBg)
                rb.rating = item.data.score.toFloat()
            }
        }

        private fun getRatingBarBgRes(rating: Int) = when {
            rating < 3 -> R.drawable.bg_circle_red
            rating < 4 -> R.drawable.bg_circle_yellow
            else -> R.drawable.bg_circle_green
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

    data class Item(val type: Int, val data: ResourceRatingModelView? = null)

    interface ActionListener {
        fun onAddResourceClicked()
    }
}