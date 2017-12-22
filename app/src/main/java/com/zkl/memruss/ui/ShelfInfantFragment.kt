package com.zkl.memruss.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookCompactor
import com.zkl.memruss.control.note.NotebookKey
import kotlinx.android.synthetic.main.fragment_shelf_infant.*
import org.jetbrains.anko.toast

class ShelfInfantFragment : Fragment(),
	NotebookCreationDialog.NotebookCreatedListener,
	NotebookImportDialog.NotebookImportedListener,
	NotebookUpgradeDialog.NotebookUpgradedListener {
	
	companion object {
		fun newInstance(): ShelfInfantFragment = ShelfInfantFragment::class.java.newInstance()
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_shelf_infant, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		b_create.setOnClickListener {
			NotebookCreationDialog.newInstance(this@ShelfInfantFragment).show(fragmentManager)
		}
		b_import.setOnClickListener {
			NotebookImportDialog.newInstance(this).show(fragmentManager)
		}
	}
	
	override fun onNotebookCreated(notebookKey: NotebookKey) {
		ShelfFragment.newInstance(false).jump(fragmentManager, false)
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
	}
	
	override fun onNotebookImported(notebookKey: NotebookKey) {
		val notebook = myApp.notebookShelf.restoreNotebook(notebookKey)
		if (notebook.version < NotebookCompactor.LATEST_VERSION) {
			NotebookUpgradeDialog.newInstance(notebookKey, this).show(fragmentManager)
		} else {
			ShelfFragment.newInstance(false).jump(fragmentManager, false)
			NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
		}
		activity.toast(R.string.import_succeed)
	}
	
	override fun onNotebookUpgraded(notebookKey: NotebookKey, upgraded: Boolean) {
		ShelfFragment.newInstance(false).jump(fragmentManager, false)
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager)
		if(upgraded) activity.toast(R.string.upgrade_succeed)
	}
	
}