/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.autonomyprofile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.AutonomyProfileModelView
import com.bitmark.autonomy.util.modelview.formatDelta
import kotlinx.android.synthetic.main.item_area_metric_body_1.view.*
import kotlinx.android.synthetic.main.item_area_metric_body_2.view.*
import kotlinx.android.synthetic.main.item_area_metric_footer.view.*
import kotlinx.android.synthetic.main.item_area_metric_guidance.view.*
import kotlinx.android.synthetic.main.item_area_metric_header.view.*
import kotlinx.android.synthetic.main.item_area_metric_sub_header.view.*


class AutonomyProfileMetricAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val HEADER = 0x00
        private const val SUB_HEADER = 0x01
        private const val BODY_YOU = 0x02
        private const val BODY_NEIGHBOR = 0x03
        private const val BODY_RESOURCE = 0x04
        private const val FOOTER = 0x05
        private const val GUIDANCE = 0x06
    }

    private val items = mutableListOf<Item>()

    private var actionListener: ActionListener? = null

    fun setActionListener(listener: ActionListener?) {
        this.actionListener = listener
    }

    fun set(data: AutonomyProfileModelView) {
        items.clear()
        items.apply {
            if (data.individualProfile != null) {
                // add individual section
                val individual = data.individualProfile
                add(Item(HEADER, R.string.you))
                add(
                    Item(
                        BODY_YOU,
                        yourData = ItemYou(individual.symptoms, individual.symptomsDelta)
                    )
                )
                add(
                    Item(
                        BODY_YOU,
                        yourData = ItemYou(
                            behaviors = individual.behaviors,
                            behaviorsDelta = individual.behaviorsDelta
                        )
                    )
                )
            } else if (data.resources != null) {
                // add resource report section
                add(Item(HEADER, R.string.report_card))
                if (data.resources.isEmpty()) {
                    add(
                        Item(
                            GUIDANCE,
                            guidanceRes = R.string.you_can_be_the_first_to_add_resources
                        )
                    )
                    add(
                        Item(
                            FOOTER,
                            footers = listOf(
                                ItemFooter(-1, -1, false),
                                ItemFooter(
                                    R.drawable.ic_add_stateful,
                                    R.string.add_resource
                                )
                            )
                        )
                    )
                } else {
                    add(
                        Item(
                            SUB_HEADER,
                            subHeaderRes = listOf(
                                R.string.resource,
                                R.string.score_0_5,
                                R.string.ratings
                            )
                        )
                    )
                    addAll(data.resources.map { res ->
                        Item(
                            BODY_RESOURCE,
                            resourceData = ItemResource(res.name, res.score, res.ratings)
                        )
                    })
                    add(
                        Item(
                            FOOTER,
                            footers = listOf(
                                ItemFooter(
                                    R.drawable.ic_down_stateful,
                                    R.string.more,
                                    data.hasMoreResources!!
                                ),
                                ItemFooter(
                                    R.drawable.ic_add_stateful,
                                    if (data.rating!!) R.string.view_your_rating else R.string.add_rating
                                )
                            )
                        )
                    )
                }
            }

            // add neighbor section
            val neighbor = data.neighborProfile
            add(Item(HEADER, R.string.neighborhood))
            add(
                Item(
                    BODY_NEIGHBOR,
                    neighborData = ItemNeighbor(neighbor.confirm, neighbor.confirmDelta)
                )
            )
            add(
                Item(
                    BODY_NEIGHBOR,
                    neighborData = ItemNeighbor(
                        symptoms = neighbor.symptoms,
                        symptomDelta = neighbor.symptomsDelta
                    )
                )
            )
            add(
                Item(
                    BODY_NEIGHBOR,
                    neighborData = ItemNeighbor(
                        behaviors = neighbor.behaviors,
                        behaviorsDelta = neighbor.behaviorsDelta
                    )
                )
            )
        }

        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> HeaderVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_metric_header,
                    parent,
                    false
                )
            )
            SUB_HEADER -> SubHeaderVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_metric_sub_header,
                    parent,
                    false
                )
            )
            BODY_YOU -> BodyYouVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_metric_body_2,
                    parent,
                    false
                ),
                actionListener
            )
            BODY_NEIGHBOR -> BodyNeighborVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_metric_body_2,
                    parent,
                    false
                ),
                actionListener
            )
            BODY_RESOURCE -> BodyResourceVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_metric_body_1,
                    parent,
                    false
                )
            )
            FOOTER -> FooterVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_metric_footer,
                    parent,
                    false
                ), actionListener
            )
            GUIDANCE -> GuidanceVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_metric_guidance,
                    parent,
                    false
                )
            )
            else -> error("unsupported viewType: $viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is HeaderVH -> holder.bind(item)
            is SubHeaderVH -> holder.bind(item)
            is BodyYouVH -> holder.bind(item)
            is BodyNeighborVH -> holder.bind(item)
            is BodyResourceVH -> holder.bind(item)
            is FooterVH -> holder.bind(item)
            is GuidanceVH -> holder.bind(item)
            else -> error("unsupported holder")
        }
    }

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            with(itemView) {
                tvTitle.setText(item.headerRes!!)
            }
        }
    }

    class SubHeaderVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            if (item.subHeaderRes!!.size != 3) error("only support 3 sub headers")
            with(itemView) {
                tvSubHeader1.setText(item.subHeaderRes[0])
                tvSubHeader2.setText(item.subHeaderRes[1])
                tvSubHeader3.setText(item.subHeaderRes[2])
            }
        }
    }

    class BodyYouVH(view: View, actionListener: ActionListener?) : RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                layoutRootBody2.setSafetyOnclickListener {
                    actionListener?.onItemClick(item)
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                val data = item.yourData!!
                if (data.symptoms != null) {
                    tvBody21.setText(R.string.symptoms)
                    tvBody22.text = data.symptoms.toString()
                    tvBody23.text = formatDelta(data.symptomDelta!!)
                    when {
                        data.symptomDelta == 0f -> {
                            tvBody23.setTextColorStateList(R.color.mine_shaft_2)
                            ivBody21.invisible()
                        }
                        data.symptomDelta < 0f -> {
                            tvBody23.setTextColorStateList(R.color.color_apple_stateful)
                            ivBody21.visible()
                            ivBody21.setImageResource(R.drawable.ic_down_green)
                        }
                        else -> {
                            tvBody23.setTextColorStateList(R.color.color_persian_red_stateful)
                            ivBody21.visible()
                            ivBody21.setImageResource(R.drawable.ic_up_red)
                        }
                    }

                } else if (data.behaviors != null) {
                    tvBody21.setText(R.string.healthy_behaviors)
                    tvBody22.text = data.behaviors.toString()
                    tvBody23.text = formatDelta(data.behaviorsDelta!!)
                    when {
                        data.behaviorsDelta == 0f -> {
                            tvBody23.setTextColorStateList(R.color.mine_shaft_2)
                            ivBody21.invisible()
                        }
                        data.behaviorsDelta < 0f -> {
                            tvBody23.setTextColorStateList(R.color.color_persian_red_stateful)
                            ivBody21.visible()
                            ivBody21.setImageResource(R.drawable.ic_down_red)
                        }
                        else -> {
                            tvBody23.setTextColorStateList(R.color.color_apple_stateful)
                            ivBody21.visible()
                            ivBody21.setImageResource(R.drawable.ic_up_green)
                        }
                    }
                }
            }
        }
    }

    class BodyNeighborVH(view: View, actionListener: ActionListener?) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                layoutRootBody2.setSafetyOnclickListener {
                    actionListener?.onItemClick(item)
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                val data = item.neighborData!!
                when {
                    data.symptoms != null -> {
                        tvBody21.setText(R.string.symptoms)
                        tvBody22.text = data.symptoms.toString()
                        tvBody23.text = formatDelta(data.symptomDelta!!)
                        when {
                            data.symptomDelta == 0f -> {
                                tvBody23.setTextColorStateList(R.color.mine_shaft_2)
                                ivBody21.invisible()
                            }
                            data.symptomDelta < 0f -> {
                                tvBody23.setTextColorStateList(R.color.color_apple_stateful)
                                ivBody21.visible()
                                ivBody21.setImageResource(R.drawable.ic_down_green)
                            }
                            else -> {
                                tvBody23.setTextColorStateList(R.color.color_persian_red_stateful)
                                ivBody21.visible()
                                ivBody21.setImageResource(R.drawable.ic_up_red)
                            }
                        }

                    }
                    data.behaviors != null -> {
                        tvBody21.setText(R.string.healthy_behaviors)
                        tvBody22.text = data.behaviors.toString()
                        tvBody23.text = formatDelta(data.behaviorsDelta!!)
                        when {
                            data.behaviorsDelta == 0f -> {
                                tvBody23.setTextColorStateList(R.color.mine_shaft_2)
                                ivBody21.invisible()
                            }
                            data.behaviorsDelta < 0f -> {
                                tvBody23.setTextColorStateList(R.color.color_persian_red_stateful)
                                ivBody21.visible()
                                ivBody21.setImageResource(R.drawable.ic_down_red)
                            }
                            else -> {
                                tvBody23.setTextColorStateList(R.color.color_apple_stateful)
                                ivBody21.visible()
                                ivBody21.setImageResource(R.drawable.ic_up_green)
                            }
                        }
                    }
                    data.confirm != null -> {

                        tvBody21.setText(R.string.active_cases)
                        tvBody22.text = data.confirm.toString()
                        tvBody23.text = formatDelta(data.confirmDelta!!)
                        when {
                            data.confirmDelta == 0f -> {
                                tvBody23.setTextColorStateList(R.color.mine_shaft_2)
                                ivBody21.invisible()
                            }
                            data.confirmDelta < 0f -> {
                                tvBody23.setTextColorStateList(R.color.color_apple_stateful)
                                ivBody21.visible()
                                ivBody21.setImageResource(R.drawable.ic_down_green)
                            }
                            else -> {
                                tvBody23.setTextColorStateList(R.color.color_persian_red_stateful)
                                ivBody21.visible()
                                ivBody21.setImageResource(R.drawable.ic_up_red)
                            }
                        }
                    }
                }
            }
        }

    }

    class BodyResourceVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            val data = item.resourceData!!
            with(itemView) {
                tvBody11.text = data.text
                tvBody12.text = String.format("%.1f", data.score)
                tvBody12.setTextColorRes(
                    when {
                        data.score == 0f -> R.color.mine_shaft_2
                        data.score <= 1.6f -> R.color.persian_red
                        data.score <= 3.3f -> R.color.gold_tip
                        else -> R.color.apple
                    }
                )
                tvBody13.text = data.ratings.abbreviate()
            }
        }
    }

    class FooterVH(view: View, listener: ActionListener?) : RecyclerView.ViewHolder(view) {

        init {
            with(itemView) {
                layoutAction1.setSafetyOnclickListener {
                    listener?.onFooterClick(layoutAction1.tvAction1.text.toString())
                }

                layoutAction2.setSafetyOnclickListener {
                    listener?.onFooterClick(layoutAction2.tvAction2.text.toString())
                }
            }
        }

        fun bind(item: Item) {
            if (item.footers!!.size != 2) error("only support 2 footer actions")
            with(itemView) {
                arrayOf(
                    Pair(ivAction1, tvAction1),
                    Pair(ivAction2, tvAction2)
                ).forEachIndexed { i, p ->
                    val f = item.footers[i]
                    if (f.iconRes != -1) p.first.setImageResource(f.iconRes)
                    if (f.stringRes != -1) p.second.setText(f.stringRes)
                    if (f.visible) {
                        p.first.visible()
                        p.second.visible()
                    } else {
                        p.first.invisible()
                        p.second.invisible()
                    }
                }
            }
        }
    }

    class GuidanceVH(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Item) {
            with(itemView) {
                tvGuidance.setText(item.guidanceRes!!)
            }
        }
    }

    data class Item(
        val type: Int,
        @StringRes val headerRes: Int? = null,
        val subHeaderRes: List<Int>? = null,
        val footers: List<ItemFooter>? = null,
        val yourData: ItemYou? = null,
        val neighborData: ItemNeighbor? = null,
        val resourceData: ItemResource? = null,
        @StringRes val guidanceRes: Int? = null
    )

    data class ItemFooter(@DrawableRes val iconRes: Int, @StringRes val stringRes: Int, var visible: Boolean = true)

    data class ItemYou(
        val symptoms: Int? = null,
        val symptomDelta: Float? = null,
        val behaviors: Int? = null,
        val behaviorsDelta: Float? = null
    )

    data class ItemNeighbor(
        val confirm: Int? = null,
        val confirmDelta: Float? = null,
        val symptoms: Int? = null,
        val symptomDelta: Float? = null,
        val behaviors: Int? = null,
        val behaviorsDelta: Float? = null
    )

    data class ItemResource(val text: String, val score: Float, val ratings: Int)

    interface ActionListener {
        fun onFooterClick(label: String)

        fun onItemClick(item: Item)
    }
}