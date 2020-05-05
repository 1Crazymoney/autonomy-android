/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.arealist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.util.ext.gone
import com.bitmark.autonomy.util.ext.move
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.ext.visible
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.modelview.toDrawableRes
import com.chauthai.swipereveallayout.ViewBinderHelper
import kotlinx.android.synthetic.main.item_area.view.*
import kotlinx.android.synthetic.main.item_area_footer.view.*
import kotlin.math.roundToInt


class AreaListRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val AREA = 0x00

        private const val FOOTER = 0x01
    }

    private val items = mutableListOf<Item>()

    private var actionListener: ActionListener? = null

    private val viewBinderHelper = ViewBinderHelper()

    fun setActionListener(listener: ActionListener) {
        this.actionListener = listener
    }

    fun get(pos: Int) = items[pos]

    fun listId() = items.filter { i -> i.type == AREA }.map { i -> i.data!!.id }

    fun set(items: List<AreaModelView>, hasFooter: Boolean = true) {
        this.items.clear()
        this.items.addAll(items.map { i -> Item(AREA, i) })
        if (hasFooter) this.items.add(Item(FOOTER))
        notifyDataSetChanged()
    }

    fun add(item: AreaModelView) {
        val pos = items.size - 1
        items.add(pos, Item(AREA, item))
        notifyItemInserted(pos)
    }

    fun updateAlias(id: String, alias: String) {
        val index = items.indexOfFirst { i -> i.data?.id == id }
        if (index != -1) {
            items[index].data?.alias = alias
            notifyItemChanged(index)
        }
    }

    fun setEditable(id: String, editable: Boolean) {
        val index = items.indexOfFirst { i -> i.data?.id == id }
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
        val index = items.indexOfFirst { i -> i.data?.id == id }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun move(fromPos: Int, toPos: Int) {
        items.move(fromPos, toPos)
        notifyItemMoved(fromPos, toPos)
    }

    fun setFooterVisibility(visible: Boolean) {
        val index = items.indexOfFirst { i -> i.type == FOOTER }
        if (visible) {
            if (index != -1) return
            val pos = items.size
            items.add(Item(FOOTER))
            notifyItemInserted(pos)
        } else {
            if (index == -1) return
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun areaCount() = items.count { i -> i.type == AREA }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == AREA) {
            AreaVH(
                LayoutInflater.from(parent.context).inflate(R.layout.item_area, parent, false),
                actionListener,
                viewBinderHelper
            )
        } else {
            FooterVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_area_footer,
                    parent,
                    false
                ), actionListener
            )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AreaVH) {
            viewBinderHelper.setOpenOnlyOne(true)
            viewBinderHelper.bind(holder.itemView.layoutRoot, items[position].data!!.id)
            holder.bind(items[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    class AreaVH(
        view: View,
        actionListener: ActionListener?,
        private val binderHelper: ViewBinderHelper
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var item: Item

        private var lastAlias = ""

        init {
            with(itemView) {

                layoutContent.setOnClickListener {
                    actionListener?.onAreaClicked(item.data!!.id)
                }

                layoutRoot.setOnTouchListener { v, _ ->
                    v.parent.parent.requestDisallowInterceptTouchEvent(true)
                    false
                }

                ivDelete.setSafetyOnclickListener {
                    actionListener?.onAreaDeleteClicked(item.data!!.id)
                    binderHelper.closeLayout(item.data!!.id)
                }

                ivEdit.setSafetyOnclickListener {
                    actionListener?.onAreaEditClicked(item.data!!.id)
                    binderHelper.closeLayout(item.data!!.id)
                }

                edtName.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val newAlias = edtName.text.toString()
                        actionListener?.onDone(item.data!!.id, lastAlias, newAlias)
                        true
                    } else false
                }

                edtName.doOnTextChanged { text, _, _, _ ->
                    item.data!!.alias = text.toString()
                    tvName.text = text
                }
            }
        }

        fun bind(item: Item) {
            this.item = item
            with(itemView) {
                val alias = item.data!!.alias
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
                } else {
                    lastAlias = ""
                    edtName.gone()
                    tvName.visible()
                    tvName.text = alias
                    edtName.clearFocus()
                }
                val score = item.data.score!!.roundToInt()
                ivScore.setImageResource(toDrawableRes(score))
                tvScore.text = score.toString()
            }
        }
    }

    class FooterVH(view: View, actionListener: ActionListener?) :
        RecyclerView.ViewHolder(view) {

        init {
            with(itemView) {
                layoutRootFooter.setOnClickListener {
                    actionListener?.onAddClicked()
                }
            }
        }
    }

    data class Item(val type: Int, val data: AreaModelView? = null, var editable: Boolean = false)

    interface ActionListener {

        fun onAreaClicked(id: String)

        fun onAreaDeleteClicked(id: String)

        fun onAreaEditClicked(id: String)

        fun onAddClicked()

        fun onDone(id: String, oldAlias: String, newAlias: String)
    }
}