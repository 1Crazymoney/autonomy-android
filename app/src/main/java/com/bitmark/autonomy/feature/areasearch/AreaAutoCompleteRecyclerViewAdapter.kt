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
import kotlinx.android.synthetic.main.item_area_autocomplete.view.*
import kotlin.math.roundToInt


class AreaAutoCompleteRecyclerViewAdapter :
    RecyclerView.Adapter<AreaAutoCompleteRecyclerViewAdapter.ViewHolder>() {

    private val items = mutableListOf<Item>()

    private var searchText = ""

    private var listener: ItemClickListener? = null

    fun set(places: List<PlaceAutoComplete>, searchText: String) {
        this.searchText = searchText
        this.items.clear()
        this.items.addAll(places.map { p ->
            Item(
                p.id,
                p.primaryText,
                p.secondaryText,
                p.desc,
                p.score
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_area_autocomplete,
                parent,
                false
            ),
            listener
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], searchText)
    }


    class ViewHolder(view: View, listener: ItemClickListener?) : RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                layoutRoot.setOnClickListener {
                    listener?.onItemClicked(item)
                }
            }
        }

        fun bind(item: Item, searchText: String) {
            this.item = item
            with(itemView) {
                val spannableString = SpannableString(item.primaryText)
                val start = item.primaryText.indexOf(searchText, ignoreCase = true)
                if (start != -1) {
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.WHITE),
                        start,
                        start + searchText.length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                tvName.text = spannableString
                tvDesc.text = item.secondaryText
                if (item.score == null) {
                    ivScore.setImageResource(R.drawable.ic_circle_mine_shaft_2)
                    tvScore.text = "?"
                } else {
                    val roundedScore = item.score.roundToInt()
                    ivScore.setImageResource(
                        when {
                            roundedScore == 0 -> R.drawable.ic_circle_mine_shaft_2
                            roundedScore < 34 -> R.drawable.ic_circle_red
                            roundedScore < 67 -> R.drawable.ic_circle_yellow
                            else -> R.drawable.ic_circle_green
                        }
                    )
                    tvScore.text = roundedScore.toString()
                }
            }
        }
    }

    interface ItemClickListener {
        fun onItemClicked(item: Item)
    }

    data class Item(
        val id: String,
        val primaryText: String,
        val secondaryText: String,
        val desc: String,
        val score: Float?
    )
}