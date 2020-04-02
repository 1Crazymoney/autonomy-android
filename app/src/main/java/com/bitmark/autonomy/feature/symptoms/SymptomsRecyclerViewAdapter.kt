/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.modelview.SymptomModelView
import kotlinx.android.synthetic.main.item_symptom.view.*


class SymptomsRecyclerViewAdapter : RecyclerView.Adapter<SymptomsRecyclerViewAdapter.ViewHolder>() {

    private val items = mutableListOf<Item>()

    private var itemsCheckedChangeListener: ItemsCheckedChangeListener? = null

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (getCheckedSymptoms().isNotEmpty()) {
            itemsCheckedChangeListener?.onChecked()
        } else {
            itemsCheckedChangeListener?.onUnChecked()
        }
    }

    fun setItemsCheckedChangeListener(listener: ItemsCheckedChangeListener) {
        this.itemsCheckedChangeListener = listener
    }

    fun set(symptoms: List<SymptomModelView>) {
        items.clear()
        items.addAll(symptoms.map { s -> Item(s) })
        notifyDataSetChanged()
    }

    fun getCheckedSymptoms() = items.filter { i -> i.checked }.map { i -> i.symptom }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_symptom,
                parent,
                false
            ),
            onCheckedChangeListener
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(view: View, onCheckedChangeListener: CompoundButton.OnCheckedChangeListener) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                cbSymptom.setOnCheckedChangeListener { view, isChecked ->
                    item.checked = isChecked
                    onCheckedChangeListener.onCheckedChanged(view, isChecked)
                }

                layoutRoot.setOnClickListener {
                    cbSymptom.isChecked = !cbSymptom.isChecked
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                tvSymptom.text = item.symptom.symptom
                tvSymptomDes.text = item.symptom.symptomDes
            }
        }
    }

    data class Item(val symptom: SymptomModelView, var checked: Boolean = false)

    interface ItemsCheckedChangeListener {

        fun onChecked()

        fun onUnChecked()
    }
}