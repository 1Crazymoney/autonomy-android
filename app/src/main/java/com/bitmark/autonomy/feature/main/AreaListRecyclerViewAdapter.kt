/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.AreaModelView
import kotlinx.android.synthetic.main.item_area.view.*
import kotlin.math.roundToInt


class AreaListRecyclerViewAdapter : RecyclerView.Adapter<AreaListRecyclerViewAdapter.AreaVH>() {

    private val items = mutableListOf<Item>()

    private var actionListener: ActionListener? = null

    fun setActionListener(listener: ActionListener) {
        this.actionListener = listener
    }

    fun get(pos: Int) = items[pos]

    fun getPos(id: String) = items.indexOfFirst { i -> i.data.id == id }

    fun set(items: List<AreaModelView>) {
        this.items.clear()
        this.items.addAll(items.map { i -> Item(i) })
        notifyDataSetChanged()
    }

    fun add(item: AreaModelView) {
        val pos = items.size
        items.add(pos, Item(item))
        notifyItemInserted(pos)
    }

    fun updateAlias(id: String, alias: String) {
        val index = items.indexOfFirst { i -> i.data.id == id }
        if (index != -1) {
            items[index].data.alias = alias
            notifyItemChanged(index)
        }
    }

    fun setEditable(id: String, editable: Boolean) {
        val index = items.indexOfFirst { i -> i.data.id == id }
        if (index != -1) {
            if (editable) {
                val editableIndex = items.indexOfFirst { i -> i.editable }
                if (editableIndex != -1) {
                    items[editableIndex].editable = false
                    notifyItemChanged(editableIndex)
                }
            }

            if (items[index].editable == editable) return
            items[index].editable = editable
            notifyItemChanged(index)
        }
    }

    fun clearEditing() {
        val index = items.indexOfFirst { i -> i.editable }
        if (index != -1) {
            items[index].editable = false
            notifyItemChanged(index)
        }
    }

    fun remove(id: String) {
        val index = items.indexOfFirst { i -> i.data.id == id }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun move(fromPos: Int, toPos: Int) {
        items.move(fromPos, toPos)
        notifyItemMoved(fromPos, toPos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaVH {
        return AreaVH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_area, parent, false),
            actionListener
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AreaVH, position: Int) {
        holder.bind(items[position])
    }

    class AreaVH(
        view: View,
        actionListener: ActionListener?
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        private var lastAlias = ""

        init {
            with(itemView) {

                layoutRoot.setOnClickListener {
                    actionListener?.onAreaClicked(item.data.id)
                }

                ivDelete.setSafetyOnclickListener {
                    actionListener?.onAreaDeleteClicked(item.data.id)
                }

                layoutRoot.setOnLongClickListener {
                    actionListener?.onAreaEditClicked(item.data.id)
                    true
                }

                edtName.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val newAlias = edtName.text.toString()
                        actionListener?.onDone(item.data.id, lastAlias, newAlias)
                        true
                    } else false
                }

                edtName.doOnTextChanged { text, _, _, _ ->
                    item.data.alias = text.toString()
                    tvName.text = text
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                val alias = item.data.alias
                if (item.editable) {
                    lastAlias = alias
                    tvName.gone()
                    edtName.visible()
                    edtName.setText(alias)
                    edtName.setSelection(alias.length)
                    edtName.setSelectAllOnFocus(true)
                    edtName.setImeActionLabel(
                        context.getString(R.string.save),
                        EditorInfo.IME_ACTION_DONE
                    )
                    edtName.requestFocus()
                    ivDelete.visible()
                } else {
                    lastAlias = ""
                    edtName.gone()
                    tvName.visible()
                    tvName.text = alias
                    edtName.clearFocus()
                    ivDelete.gone()
                }
                val score = item.data.score!!.roundToInt()
                ivScore.setImageResource("triangle_%03d".format(if (score > 100) 100 else score))
                tvScore.text = score.toString()
            }
        }
    }

    data class Item(var data: AreaModelView, var editable: Boolean = false)

    interface ActionListener {

        fun onAreaClicked(id: String?)

        fun onAreaDeleteClicked(id: String)

        fun onAreaEditClicked(id: String)

        fun onDone(id: String, oldAlias: String, newAlias: String)
    }
}