/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.trending

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.ReportType
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.ReportItemModelView
import com.bitmark.autonomy.util.modelview.formatDelta
import com.bitmark.autonomy.util.modelview.isNotSupported
import com.bitmark.autonomy.util.modelview.scoreToColorRes
import kotlinx.android.synthetic.main.item_trending.view.*
import kotlinx.android.synthetic.main.item_trending.view.layoutRoot
import kotlinx.android.synthetic.main.item_trending_header.view.*
import java.util.*
import kotlin.math.roundToInt


class TrendingRecyclerViewAdapter(private val highlightEnable: Boolean = true) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val MAX_COLOR = 6

        private const val HEADER = 0x00

        private const val BODY = 0x01

        internal const val DEFAULT_COLOR = "mine_shaft_2"
    }

    private val items = mutableListOf<Item>()

    private var itemHighlightListener: (() -> Unit)? = null

    private val colorArray =
        arrayOf("mabilu", "chenin", "carissma", "tea_green", "apricot", "perfume")

    private val removedColorQueue = ArrayDeque<String>()

    private var colorIndex = 0

    fun setItemHighlightListener(listener: () -> Unit) {
        this.itemHighlightListener = listener
    }

    fun isEmpty() = items.isEmpty()

    private val internalItemClickListener: (Item) -> Unit = { item ->
        if (highlightEnable) {
            if (item.colorName == DEFAULT_COLOR) {
                val popFromRemovedQueue = removedColorQueue.isNotEmpty()
                val color =
                    if (popFromRemovedQueue) removedColorQueue.poll() else colorArray[colorIndex++ % MAX_COLOR]

                if (getHighlightCount() == MAX_COLOR) {
                    val oldPos = items.indexOfFirst { i -> i.colorName == color }
                    items[oldPos].colorName = DEFAULT_COLOR
                    notifyItemChanged(oldPos)
                }

                val newPos = items.indexOf(item)
                item.colorName = color
                notifyItemChanged(newPos)
                itemHighlightListener?.invoke()
            } else if (item.colorName != null) {
                removedColorQueue.add(item.colorName)
                item.colorName = DEFAULT_COLOR
                val pos = items.indexOf(item)
                notifyItemChanged(pos)
                itemHighlightListener?.invoke()
            }
        }
    }

    private fun getHighlightCount() =
        items.count { i -> i.colorName != null && i.colorName != DEFAULT_COLOR }

    fun getHighLightItems() =
        mapOf(*items.filter { i -> i.type == BODY && i.colorName != null && i.colorName != DEFAULT_COLOR }.map {
            Pair(
                it.data!!.id,
                it.colorName!!
            )
        }.toTypedArray())

    fun clear() {
        this.items.clear()
        clearColorHandler()
        notifyDataSetChanged()
    }

    private fun clearColorHandler() {
        removedColorQueue.clear()
        colorIndex = 0
    }

    fun set(items: List<ReportItemModelView>) {
        if (items.isEmpty()) return

        val header = when (val type = items[0].type) {
            ReportType.SYMPTOM.value -> Header(R.string.symptoms, R.string.days, R.string.change)
            ReportType.BEHAVIOR.value -> Header(
                R.string.healthy_behaviors,
                R.string.times,
                R.string.change
            )
            else -> error("unsupported type: $type")
        }

        this.items.clear()
        clearColorHandler()
        this.items.add(Item(HEADER, header, null, null))
        this.items.addAll(items.map { i ->
            Item(BODY, null, if (i.value != null && i.value > 0f) DEFAULT_COLOR else null, i)
        })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER) {
            HeaderVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_trending_header,
                    parent,
                    false
                )
            )
        } else {
            BodyVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_trending,
                    parent,
                    false
                ),
                internalItemClickListener
            )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderVH) {
            holder.bind(items[position])
        } else if (holder is BodyVH) {
            holder.bind(items[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            with(itemView) {
                tv1.setText(item.header!!.text1)
                tv2.setText(item.header.text2)
                tv3.setText(item.header.text3)
            }
        }
    }

    class BodyVH(view: View, itemClickListener: (Item) -> Unit) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                layoutRoot.setOnClickListener {
                    itemClickListener(item)
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                val data = item.data
                tvName.text = data!!.name
                if (item.colorName == null) {
                    ivBadge.invisible()
                } else {
                    ivBadge.visible()
                    ivBadge.setImageResource(
                        context.getDrawableIdentifier("ic_circle_${item.colorName}")
                    )
                }

                if (!data.isNotSupported()) {
                    tvDelta.text = data.changeRate!!.formatDelta()
                    when (data.type) {
                        ReportType.SCORE.value -> {
                            val value = data.value!!.roundToInt()
                            tvValue.setTextColorRes(value.scoreToColorRes())
                            tvValue.text = value.toString()
                            val scoreDelta = data.changeRate
                            when {
                                scoreDelta == 0f -> {
                                    ivDelta.invisible()
                                    tvDelta.setTextColorStateList(R.color.color_concord_stateful)
                                }

                                scoreDelta < 0f -> {
                                    ivDelta.visible()
                                    tvDelta.setTextColorStateList(R.color.color_persian_red_stateful)
                                    ivDelta.setImageResource(R.drawable.ic_down_red)
                                }

                                else -> {
                                    ivDelta.visible()
                                    tvDelta.setTextColorStateList(R.color.color_apple_stateful)
                                    ivDelta.setImageResource(R.drawable.ic_up_green)
                                }
                            }
                        }

                        ReportType.SYMPTOM.value, ReportType.CASE.value -> {
                            tvValue.setTextColorRes(R.color.white)
                            tvValue.text = data.value!!.toInt().toString()
                            val delta = data.changeRate
                            when {
                                delta == 0f -> {
                                    tvDelta.setTextColorStateList(R.color.color_concord_stateful)
                                    ivDelta.invisible()
                                }
                                delta < 0f -> {
                                    tvDelta.setTextColorStateList(R.color.color_apple_stateful)
                                    ivDelta.visible()
                                    ivDelta.setImageResource(R.drawable.ic_down_green)
                                }
                                else -> {
                                    tvDelta.setTextColorStateList(R.color.color_persian_red_stateful)
                                    ivDelta.visible()
                                    ivDelta.setImageResource(R.drawable.ic_up_red)
                                }
                            }
                        }

                        ReportType.BEHAVIOR.value -> {
                            tvValue.setTextColorRes(R.color.white)
                            tvValue.text = data.value!!.toInt().toString()
                            val behaviorDelta = data.changeRate
                            when {
                                behaviorDelta == 0f -> {
                                    tvDelta.setTextColorStateList(R.color.color_concord_stateful)
                                    ivDelta.invisible()
                                }
                                behaviorDelta < 0f -> {
                                    tvDelta.setTextColorStateList(R.color.color_persian_red_stateful)
                                    ivDelta.visible()
                                    ivDelta.setImageResource(R.drawable.ic_down_red)
                                }
                                else -> {
                                    tvDelta.setTextColorStateList(R.color.color_apple_stateful)
                                    ivDelta.visible()
                                    ivDelta.setImageResource(R.drawable.ic_up_green)
                                }
                            }
                        }

                        else -> error("unsupported type ${data.type}")
                    }
                } else {
                    tvDelta.text = "0.00%"
                    tvDelta.setTextColorStateList(R.color.color_concord_stateful)
                    tvValue.text = "--"
                    tvValue.setTextColorStateList(R.color.color_white_stateful)
                }

            }
        }
    }

    data class Item(
        val type: Int,
        val header: Header?,
        var colorName: String?,
        val data: ReportItemModelView?
    )

    data class Header(@StringRes val text1: Int, @StringRes val text2: Int, @StringRes val text3: Int)
}