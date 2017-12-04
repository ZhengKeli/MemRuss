package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookBrief
import com.zkl.zklRussian.control.note.NotebookKey
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.find

class NotebookShelfFragment : Fragment()
	, NotebookMenuDialog.NotebookListChangedListener
	, NotebookCreationDialog.NotebookCreatedListener {
	
	companion object {
		private val arg_autoJump: String = "autoJump"
		fun newInstance(autoJumpToFirst: Boolean)
			= NotebookShelfFragment().apply {
			arguments = bundleOf(
				arg_autoJump to autoJumpToFirst
			)
		}
		
		private var NotebookShelfFragment.autoJump: Boolean
			get() = arguments!!.getBoolean(arg_autoJump)
			set(value) {
				arguments!!.putBoolean(arg_autoJump, value)
			}
	}
	
	private class NotebookItemView(context: Context) : LinearLayout(context) {
		private val tv_notebookName: TextView
		
		init {
			LayoutInflater.from(context).inflate(R.layout.adapter_notebook_item, this, true)
			tv_notebookName = find(R.id.tv_notebookName)
		}
		
		var notebookBrief: NotebookBrief? = null
			set(value) {
				field = value
				tv_notebookName.text = value?.bookName ?: ""
			}
	}
	
	lateinit var b_create: Button
	lateinit var b_import: Button
	lateinit var b_export: Button
	lateinit var lv_notebooks: ListView
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook_shelf, container, false).apply {
		b_create = find<Button>(R.id.b_create)
		b_import = find<Button>(R.id.b_import)
		b_export = find<Button>(R.id.b_merge)
		lv_notebooks = find<ListView>(R.id.lv_notebooks)
	}.apply {
		briefs = myApp.notebookShelf.loadBookSummaries()
		if (briefs.isEmpty()) NotebookInfantFragment().jump(fragmentManager, true)
		else if (autoJump) {
			val (key, _) = myApp.notebookShelf.openMutableNotebook(briefs.first().file)
			NotebookFragment.newInstance(key).jump(fragmentManager, true)
			autoJump = false
		}
		
		b_create.setOnClickListener {
			NotebookCreationDialog.newInstance(this@NotebookShelfFragment).show(fragmentManager, null)
		}
		b_import.setOnClickListener {
			TODO()
		}
		b_export.setOnClickListener {
			TODO()
		}
		
		lv_notebooks.adapter = object : BaseAdapter() {
			override fun getView(position: Int, convertView: View?, parent: ViewGroup?): NotebookItemView
				= ((convertView as? NotebookItemView) ?: NotebookItemView(context)).apply { notebookBrief = getItem(position) }
			
			override fun getCount() = briefs.size
			
			override fun getItem(position: Int) = briefs[position]
			
			override fun getItemId(position: Int) = 0L
			
		}
		lv_notebooks.setOnItemClickListener { parent, _, position, _ ->
			val summary = parent.adapter.getItem(position) as NotebookBrief
			val (key, _) = myApp.notebookShelf.openMutableNotebook(summary.file)
			NotebookFragment.newInstance(key).jump(fragmentManager, true)
		}
		lv_notebooks.setOnItemLongClickListener { parent, _, position, _ ->
			val summary = parent.adapter.getItem(position) as NotebookBrief
			NotebookMenuDialog.newInstance(summary, this@NotebookShelfFragment).show(fragmentManager, null)
			true
		}
	}
	
	override fun onNotebookListChanged() {
		briefs = myApp.notebookShelf.loadBookSummaries()
		if (briefs.isEmpty()) NotebookInfantFragment().jump(fragmentManager, true)
		(lv_notebooks.adapter as? BaseAdapter)?.notifyDataSetChanged()
	}
	
	override fun onNotebookCreated(notebookKey: NotebookKey) {
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager, true)
	}
	
	//summaries
	var briefs: List<NotebookBrief> = emptyList()
	
}


