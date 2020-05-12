/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.add2

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.modelview.BehaviorModelView
import kotlinx.android.synthetic.main.item_simple_autocomplete.view.*


class AutocompleteRecyclerViewAdapter : RecyclerView.Adapter<AutocompleteRecyclerViewAdapter.VH>() {

    private val items = mutableListOf<BehaviorModelView>()

    private var itemClickListener: ((BehaviorModelView) -> Unit)? = null

    private var searchText = ""

    fun setItemClickListener(listener: (BehaviorModelView) -> Unit) {
        this.itemClickListener = listener
    }

    fun set(items: List<BehaviorModelView>, searchText: String) {
        this.searchText = searchText
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_simple_autocomplete, parent, false
        ), itemClickListener
    )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], searchText)
    }

    class VH(view: View, itemClickListener: ((BehaviorModelView) -> Unit)?) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: BehaviorModelView

        init {
            with(itemView) {
                layoutRoot.setOnClickListener {
                    itemClickListener?.invoke(item)
                }
            }
        }

        fun bind(item: BehaviorModelView, searchText: String) {
            this.item = item
            with(itemView) {
                val spannableString = SpannableString(item.name)
                val start = item.name.indexOf(searchText, ignoreCase = true)
                if (start != -1) {
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.WHITE),
                        start,
                        start + searchText.length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                tvName.text = spannableString
            }
        }
    }
}