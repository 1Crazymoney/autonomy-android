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
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.ReportType
import com.bitmark.autonomy.util.ext.invisible
import com.bitmark.autonomy.util.ext.setTextColorRes
import com.bitmark.autonomy.util.ext.visible
import com.bitmark.autonomy.util.modelview.ReportItemModelView
import com.bitmark.autonomy.util.modelview.formatDelta
import com.bitmark.autonomy.util.modelview.isNotSupported
import com.bitmark.autonomy.util.modelview.scoreToColorRes
import kotlinx.android.synthetic.main.item_trending.view.*
import kotlin.math.roundToInt


class TrendingRecyclerViewAdapter : RecyclerView.Adapter<TrendingRecyclerViewAdapter.VH>() {

    private val items = mutableListOf<ReportItemModelView>()

    fun set(items: List<ReportItemModelView>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_trending,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: ReportItemModelView) {
            with(itemView) {
                tvName.text = item.name

                if (!item.isNotSupported()) {
                    tvDelta.text = item.changeRate!!.formatDelta()
                    when (item.type) {
                        ReportType.SCORE.value -> {
                            val value = item.value!!.roundToInt()
                            tvValue.setTextColorRes(value.scoreToColorRes())
                            tvValue.text = value.toString()
                            val scoreDelta = item.changeRate
                            when {
                                scoreDelta == 0f -> {
                                    ivDelta.invisible()
                                    tvDelta.setTextColorRes(R.color.concord)
                                }

                                scoreDelta < 0f -> {
                                    ivDelta.visible()
                                    tvDelta.setTextColorRes(R.color.persian_red)
                                    ivDelta.setImageResource(R.drawable.ic_down_red)
                                }

                                else -> {
                                    ivDelta.visible()
                                    tvDelta.setTextColorRes(R.color.apple)
                                    ivDelta.setImageResource(R.drawable.ic_up_green)
                                }
                            }
                        }

                        ReportType.SYMPTOM.value, ReportType.CASE.value -> {
                            tvValue.setTextColorRes(R.color.white)
                            tvValue.text = item.value!!.toInt().toString()
                            val delta = item.changeRate
                            when {
                                delta == 0f -> {
                                    tvDelta.setTextColorRes(R.color.concord)
                                    ivDelta.invisible()
                                }
                                delta < 0f -> {
                                    tvDelta.setTextColorRes(R.color.apple)
                                    ivDelta.visible()
                                    ivDelta.setImageResource(R.drawable.ic_down_green)
                                }
                                else -> {
                                    tvDelta.setTextColorRes(R.color.persian_red)
                                    ivDelta.visible()
                                    ivDelta.setImageResource(R.drawable.ic_up_red)
                                }
                            }
                        }

                        ReportType.BEHAVIOR.value -> {
                            tvValue.setTextColorRes(R.color.white)
                            tvValue.text = item.value!!.toInt().toString()
                            val behaviorDelta = item.changeRate
                            when {
                                behaviorDelta == 0f -> {
                                    tvDelta.setTextColorRes(R.color.concord)
                                    ivDelta.invisible()
                                }
                                behaviorDelta < 0f -> {
                                    tvDelta.setTextColorRes(R.color.persian_red)
                                    ivDelta.visible()
                                    ivDelta.setImageResource(R.drawable.ic_down_red)
                                }
                                else -> {
                                    tvDelta.setTextColorRes(R.color.apple)
                                    ivDelta.visible()
                                    ivDelta.setImageResource(R.drawable.ic_up_green)
                                }
                            }
                        }

                        else -> error("unsupported type ${item.type}")
                    }
                } else {
                    tvDelta.text = "0.00%"
                    tvDelta.setTextColorRes(R.color.concord)
                    tvValue.text = "--"
                    tvValue.setTextColorRes(R.color.white)
                }

            }
        }
    }
}