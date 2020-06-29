/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.areasearch

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.location.PlaceAutoComplete
import com.bitmark.autonomy.util.ext.invisible
import com.bitmark.autonomy.util.ext.setTextColorRes
import com.bitmark.autonomy.util.ext.setTextColorStateList
import com.bitmark.autonomy.util.ext.visible
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.modelview.ratingScoreToColorRes
import com.bitmark.autonomy.util.modelview.ratingToDrawableRes
import kotlinx.android.synthetic.main.item_area_autocomplete.view.*
import kotlin.math.roundToInt


class AreaAutoCompleteRecyclerViewAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        internal const val AUTOCOMPLETE_PLACE = 0x00
        internal const val RESOURCE_PLACE = 0x01
    }

    private val items = mutableListOf<Item>()

    private var searchText: String = ""

    private var listener: ItemClickListener? = null

    fun setAutocompletePlaces(places: List<PlaceAutoComplete>, searchText: String) {
        this.searchText = searchText
        this.items.clear()
        this.items.addAll(places.map { p ->
            Item(
                AUTOCOMPLETE_PLACE,
                p,
                null
            )
        })
        notifyDataSetChanged()
    }

    fun setResourcePlaces(areas: List<AreaModelView>) {
        this.items.clear()
        this.items.addAll(areas.map { a ->
            Item(
                RESOURCE_PLACE,
                null,
                a
            )
        })
        notifyDataSetChanged()
    }

    fun clear() {
        this.items.clear()
        notifyDataSetChanged()
    }

    fun setItemClickListener(listener: ItemClickListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            AUTOCOMPLETE_PLACE -> AutocompletePlaceVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_autocomplete,
                    parent,
                    false
                ),
                listener
            )
            RESOURCE_PLACE -> ResourcePlaceVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_autocomplete,
                    parent,
                    false
                ),
                listener
            )
            else -> error("invalid view type: $viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AutocompletePlaceVH) {
            holder.bind(items[position], searchText)
        } else if (holder is ResourcePlaceVH) {
            holder.bind(items[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }


    class AutocompletePlaceVH(view: View, listener: ItemClickListener?) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                rb.invisible()
                tvScore.invisible()

                layoutRoot.setOnClickListener {
                    listener?.onItemClicked(item)
                }
            }
        }

        fun bind(item: Item, searchText: String) {
            this.item = item
            val place = item.place!!
            with(itemView) {
                val spannableString = SpannableString(place.primaryText)
                val start = place.primaryText!!.indexOf(searchText, ignoreCase = true)
                if (start != -1) {
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.WHITE),
                        start,
                        start + searchText.length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                tvName.text = spannableString
                tvDesc.text = place.secondaryText
            }
        }
    }

    class ResourcePlaceVH(view: View, listener: ItemClickListener?) :
        RecyclerView.ViewHolder(view) {
        private lateinit var item: Item

        init {
            with(itemView) {
                rb.visible()
                tvScore.visible()
                tvName.setTextColorStateList(R.color.color_white_stateful)

                layoutRoot.setOnClickListener {
                    listener?.onItemClicked(item)
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            val area = item.area!!
            with(itemView) {
                tvName.text = if (area.distance != null) {
                    String.format("%s (%.1f km)", area.alias, area.distance)
                } else {
                    area.alias
                }
                tvDesc.text = area.address

                if (area.resourceScore == null) {
                    tvScore.text = "--"
                    tvScore.setTextColorRes(R.color.concord)
                } else {
                    tvScore.text = String.format("%.1f", area.resourceScore)
                    rb.rating = area.resourceScore.roundToInt().toFloat()
                    rb.setFilledDrawableRes(area.resourceScore.roundToInt().ratingToDrawableRes())
                    tvScore.setTextColorRes(area.resourceScore.ratingScoreToColorRes())
                }
            }
        }
    }

    interface ItemClickListener {
        fun onItemClicked(item: Item)
    }

    data class Item(
        val type: Int,
        val place: PlaceAutoComplete?,
        val area: AreaModelView?
    )
}