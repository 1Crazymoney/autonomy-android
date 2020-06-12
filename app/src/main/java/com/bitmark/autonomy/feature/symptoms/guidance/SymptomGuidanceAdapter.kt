/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.guidance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.modelview.InstitutionModelView
import kotlinx.android.synthetic.main.item_symptom_guidance.view.*


class SymptomGuidanceAdapter : RecyclerView.Adapter<SymptomGuidanceAdapter.VH>() {

    private val items = mutableListOf<InstitutionModelView>()

    private var actionListener: ActionListener? = null

    fun setActionListener(listener: ActionListener) {
        this.actionListener = listener
    }

    fun set(items: List<InstitutionModelView>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_symptom_guidance,
            parent,
            false
        ), actionListener
    )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(view: View, listener: ActionListener?) : RecyclerView.ViewHolder(view) {

        private lateinit var item: InstitutionModelView

        init {
            with(itemView) {
                ivDirect.setSafetyOnclickListener {
                    listener?.onDirectClicked(item.address)
                }

                ivPhoneCall.setSafetyOnclickListener {
                    listener?.onPhoneCallClicked(item.phone)
                }
            }
        }

        fun bind(item: InstitutionModelView) {
            this.item = item
            with(itemView) {
                if (item.country.isEmpty()) {
                    tvName.text = item.name
                } else {
                    tvName.text = String.format("%s (%s)", item.name, item.county)
                }
                tvAddress.text = item.address
                tvDistance.text = String.format("%.1f km", item.distance)
            }
        }
    }

    interface ActionListener {
        fun onDirectClicked(address: String)

        fun onPhoneCallClicked(phoneNumber: String)
    }
}