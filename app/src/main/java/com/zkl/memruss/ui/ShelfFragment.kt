package com.zkl.memruss.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookBrief
import com.zkl.memruss.control.note.NotebookCompactor
import com.zkl.memruss.control.note.NotebookKey
import kotlinx.android.synthetic.main.fragment_shelf.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast

class ShelfFragment : Fragment(),
	NotebookCreationDialog.NotebookCreatedListener,
	NotebookDeleteDialog.NotebookDeletedListener,
	NotebookImportDialog.NotebookImportedListener,
	NotebookMergeFragment.NotebookMergedListener,
	NotebookUpgradeDialog.NotebookUpgradedListener {
	
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
		if (briefs.isEmpty()) ShelfInfantFragment.newInstance().jump(fragmentManager, false)
		else if (autoJump) {
			val (key, _) = myApp.notebookShelf.openNotebook(briefs.first().file)
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
			val (key, _) = myApp.notebookShelf.openNotebook(brief.file)
			NotebookFragment.newInstance(key).jump(fragmentManager)
		}
		lv_notebooks.setOnItemLongClickListener { parent, _, position, _ ->
			val brief = parent.adapter.getItem(position) as NotebookBrief
			NotebookMenuDialog.newInstance(brief, this@ShelfFragment).show(fragmentManager)
			true
		}
	}
	
	override fun onNotebookDeleted(){
		briefs = myApp.notebookShelf.loadNotebookBriefs()
		if (briefs.isEmpty()) ShelfInfantFragment.newInstance().jump(fragmentManager, false)
		(lv_notebooks.adapter as? BaseAdapter)?.notifyDataSetChanged()
	}
	
	override fun onNotebookCreated(notebookKey: NotebookKey) {
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
	}
	
	override fun onNotebookImported(notebookKey: NotebookKey) {
		val notebook = myApp.notebookShelf.restoreNotebook(notebookKey)
		if (notebook.version < NotebookCompactor.LATEST_VERSION) {
			NotebookUpgradeDialog.newInstance(notebookKey, this).show(fragmentManager)
		} else {
			NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
		}
		activity.toast(R.string.import_succeed)
	}
	
	override fun onNotebookMerged(notebookKey: NotebookKey) {
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
	}
	
	override fun onNotebookUpgraded(notebookKey: NotebookKey, upgraded: Boolean) {
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
		if(upgraded) activity.toast(R.string.upgrade_succeed)
	}
	
	
	//summaries
	var briefs: List<NotebookBrief> = emptyList()
	
}
