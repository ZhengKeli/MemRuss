package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookBrief
import com.zkl.zklRussian.control.note.NotebookKey
import kotlinx.android.synthetic.main.fragment_shelf.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.support.v4.toast

class ShelfFragment : Fragment(),
	NotebookMenuDialog.NotebookListChangedListener,
	NotebookCreationDialog.NotebookCreatedListener,
	NotebookImportDialog.NotebookImportedListener,
NotebookMergeFragment.NotebookMergedListener{
	
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
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_shelf, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		briefs = myApp.notebookShelf.loadNotebookBriefs()
		if (briefs.isEmpty()) ShelfInfantFragment.newInstance().jump(fragmentManager)
		else if (autoJump) {
			val (key, _) = myApp.notebookShelf.openMutableNotebook(briefs.first().file)
			NotebookFragment.newInstance(key).jump(fragmentManager)
			autoJump = false
		}
		
		b_create.setOnClickListener {
			NotebookCreationDialog.newInstance(this).show(fragmentManager)
		}
		b_import.setOnClickListener {
			NotebookImportDialog.newInstance(this).show(fragmentManager)
		}
		b_merge.setOnClickListener{
			if (briefs.size > 1) NotebookMergeFragment.newInstance(this).jump(fragmentManager)
			else toast(getString(R.string.only_one_notebook))
		}
		
		lv_notebooks.adapter = object : NotebookListAdapter() {
			override fun getCount() = briefs.size
			override fun getItem(position: Int) = briefs[position]
			override val context: Context get() = activity
		}
		lv_notebooks.setOnItemClickListener { parent, _, position, _ ->
			val brief = parent.adapter.getItem(position) as NotebookBrief
			val (key, _) = myApp.notebookShelf.openMutableNotebook(brief.file)
			NotebookFragment.newInstance(key).jump(fragmentManager)
		}
		lv_notebooks.setOnItemLongClickListener { parent, _, position, _ ->
			val brief = parent.adapter.getItem(position) as NotebookBrief
			NotebookMenuDialog.newInstance(brief, this@ShelfFragment).show(fragmentManager)
			true
		}
	}
	
	override fun onNotebookListChanged() {
		briefs = myApp.notebookShelf.loadNotebookBriefs()
		if (briefs.isEmpty()) ShelfInfantFragment().jump(fragmentManager)
		(lv_notebooks.adapter as? BaseAdapter)?.notifyDataSetChanged()
	}
	
	override fun onNotebookCreated(notebookKey: NotebookKey) {
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
	}
	
	override fun onNotebookImported(notebookKey: NotebookKey) {
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
	}
	
	override fun onNotebookMerged(notebookKey: NotebookKey) {
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
	}
	
	//summaries
	var briefs: List<NotebookBrief> = emptyList()
	
}
