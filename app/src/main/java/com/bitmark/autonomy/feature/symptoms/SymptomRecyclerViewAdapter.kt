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
import kotlinx.android.synthetic.main.item_symptom_footer.view.*


class SymptomRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val BODY = 0x00

        private const val FOOTER = 0x01
    }

    private val items = mutableListOf<Item>()

    private var itemClickListener: ItemClickListener? = null

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        if (getCheckedSymptoms().isNotEmpty()) {
            itemClickListener?.onChecked()
        } else {
            itemClickListener?.onUnChecked()
        }
    }

    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    fun set(symptoms: List<SymptomModelView>) {
        items.clear()
        items.addAll(symptoms.map { s -> Item(BODY, s, checked = false, checkable = true) })
        items.add(Item(FOOTER, null, null))
        notifyDataSetChanged()
    }

    fun add(symptom: SymptomModelView, checked: Boolean = true, checkable: Boolean = true) {
        val pos = items.size - 1
        items.add(pos, Item(BODY, symptom, checked, checkable))
        notifyItemInserted(pos)
    }

    fun getCheckedSymptoms() =
        items.filter { i -> i.type == BODY && i.checked!! }.map { i -> i.symptom }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == BODY) {
            BodyVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_symptom,
                    parent,
                    false
                ),
                onCheckedChangeListener
            )
        } else {
            FooterVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_symptom_footer,
                    parent,
                    false
                ), itemClickListener
            )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (items[position].type == BODY) {
            (holder as BodyVH).bind(items[position])
        }

    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    class BodyVH(view: View, onCheckedChangeListener: CompoundButton.OnCheckedChangeListener) :
        RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        init {
            with(itemView) {
                cbSymptom.setOnCheckedChangeListener { view, isChecked ->
                    item.checked = isChecked
                    onCheckedChangeListener.onCheckedChanged(view, isChecked)
                }

                layoutRoot.setOnClickListener {
                    if (!item.checkable!!) return@setOnClickListener
                    cbSymptom.isChecked = !cbSymptom.isChecked
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                cbSymptom.isEnabled = item.checkable!!
                cbSymptom.isChecked = item.checked!!
                tvSymptom.text = item.symptom!!.symptom
                tvSymptomDes.text = item.symptom.symptomDes
            }
        }
    }

    class FooterVH(view: View, itemClickListener: ItemClickListener?) :
        RecyclerView.ViewHolder(view) {

        init {
            with(itemView) {
                layoutRootFooter.setOnClickListener {
                    itemClickListener?.onAddNew()
                }
            }
        }

    }

    data class Item(
        val type: Int,
        val symptom: SymptomModelView?,
        var checked: Boolean? = null,
        var checkable: Boolean? = null
    )

    interface ItemClickListener {

        fun onChecked()

        fun onUnChecked()

        fun onAddNew()
    }
}