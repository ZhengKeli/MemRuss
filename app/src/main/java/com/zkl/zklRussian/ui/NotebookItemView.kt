package com.zkl.zklRussian.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookBrief
import kotlinx.android.synthetic.main.adapter_notebook_item.view.*

class NotebookItemView(context: Context) : LinearLayout(context) {
	
	init {
		LayoutInflater.from(context).inflate(R.layout.adapter_notebook_item, this, true)
	}
	
	var notebookBrief: NotebookBrief? = null
		set(value) {
			field = value
			tv_notebookName.text = value?.bookName ?: ""
		}
	
}

abstract class NotebookListAdapter : BaseAdapter() {
	abstract override fun getCount(): Int
	abstract override fun getItem(position: Int):NotebookBrief
	override fun getItemId(position: Int) = 0L
	
	abstract val context:Context
	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): NotebookItemView
		= ((convertView as? NotebookItemView) ?: NotebookItemView(context)).apply { notebookBrief = getItem(position) }
}

