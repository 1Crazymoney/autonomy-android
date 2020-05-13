/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.guidance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.ext.load
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.modelview.GuidanceModelView
import com.bitmark.autonomy.util.modelview.previewUrl
import kotlinx.android.synthetic.main.item_guidance.view.*


class GuidanceRecyclerViewAdapter : RecyclerView.Adapter<GuidanceRecyclerViewAdapter.VH>() {

    private val items = mutableListOf<GuidanceModelView>()

    private var itemClickListener: ItemClickListener? = null

    fun setItemClickListener(listener: ItemClickListener) {
        itemClickListener = listener
    }

    fun set(items: List<GuidanceModelView>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_guidance, parent, false),
        itemClickListener
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(view: View, itemClickListener: ItemClickListener?) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: GuidanceModelView

        init {
            with(itemView) {
                ivPlay.setSafetyOnclickListener {
                    itemClickListener?.onPlayClick(item.videoUrl)
                }
            }
        }

        fun bind(item: GuidanceModelView) {
            this.item = item
            with(itemView) {
                tvTitle.setText(item.title)
                ivPreview.load(item.previewUrl)
            }
        }
    }

    interface ItemClickListener {
        fun onPlayClick(url: String)
    }

}