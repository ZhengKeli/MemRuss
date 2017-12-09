package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookBrief
import com.zkl.zklRussian.control.note.NotebookKey
import kotlinx.android.synthetic.main.adapter_notebook_item.view.*
import kotlinx.android.synthetic.main.fragment_shelf.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.support.v4.toast

class ShelfFragment : Fragment(),
	NotebookMenuDialog.NotebookListChangedListener,
	NotebookCreationDialog.NotebookCreatedListener,
	NotebookImportDialog.NotebookImportedListener {
	
	companion object {
		private val arg_autoJump: String = "autoJump"
		fun newInstance(autoJumpToFirst: Boolean)
			= ShelfFragment().apply {
			arguments = bundleOf(
				arg_autoJump to autoJumpToFirst
			)
		}
		
		private var ShelfFragment.autoJump: Boolean
			get() = arguments!!.getBoolean(arg_autoJump)
			set(value) {
				arguments!!.putBoolean(arg_autoJump, value)
			}
	}
	
	private class NotebookItemView(context: Context) : LinearLayout(context) {
		init {
			LayoutInflater.from(context).inflate(R.layout.adapter_notebook_item, this, true)
		}
		
		var notebookBrief: NotebookBrief? = null
			set(value) {
				field = value
				tv_notebookName.text = value?.bookName ?: ""
			}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_shelf, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		briefs = myApp.notebookShelf.loadNotebookBriefs()
		if (briefs.isEmpty()) ShelfInfantFragment().jump(fragmentManager, true)
		else if (autoJump) {
			val (key, _) = myApp.notebookShelf.openMutableNotebook(briefs.first().file)
			NotebookFragment.newInstance(key).jump(fragmentManager, true)
			autoJump = false
		}
		
		b_create.setOnClickListener {
			NotebookCreationDialog.newInstance(this).show(fragmentManager)
		}
		b_import.setOnClickListener {
			NotebookImportDialog.newInstance(this).show(fragmentManager)
		}
		b_merge.setOnClickListener{
			toast("Not available yet!")
			//todo implement this!
		}
		
		lv_notebooks.adapter = object : BaseAdapter() {
			override fun getView(position: Int, convertView: View?, parent: ViewGroup?): NotebookItemView
				= ((convertView as? NotebookItemView) ?: NotebookItemView(context)).apply { notebookBrief = getItem(position) }
			
			override fun getCount() = briefs.size
			
			override fun getItem(position: Int) = briefs[position]
			
			override fun getItemId(position: Int) = 0L
			
		}
		lv_notebooks.setOnItemClickListener { parent, _, position, _ ->
			val brief = parent.adapter.getItem(position) as NotebookBrief
			val (key, _) = myApp.notebookShelf.openMutableNotebook(brief.file)
			NotebookFragment.newInstance(key).jump(fragmentManager, true)
		}
		lv_notebooks.setOnItemLongClickListener { parent, _, position, _ ->
			val brief = parent.adapter.getItem(position) as NotebookBrief
			NotebookMenuDialog.newInstance(brief, this@ShelfFragment).show(fragmentManager)
			true
		}
	}
	
	override fun onNotebookListChanged() {
		briefs = myApp.notebookShelf.loadNotebookBriefs()
		if (briefs.isEmpty()) ShelfInfantFragment().jump(fragmentManager, true)
		(lv_notebooks.adapter as? BaseAdapter)?.notifyDataSetChanged()
	}
	
	override fun onNotebookCreated(notebookKey: NotebookKey) {
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager, true)
	}
	
	override fun onNotebookImported(notebookKey: NotebookKey) {
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager, true)
	}
	
	//summaries
	var briefs: List<NotebookBrief> = emptyList()
	
}


