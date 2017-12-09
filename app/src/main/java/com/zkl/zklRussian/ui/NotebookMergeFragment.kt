package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import kotlinx.android.synthetic.main.fragment_shelf_infant.*

class NotebookMergeFragment : Fragment(),
	NotebookCreationDialog.NotebookCreatedListener,
	NotebookImportDialog.NotebookImportedListener{
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_shelf_infant, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		b_create.setOnClickListener {
			NotebookCreationDialog.newInstance(this@NotebookMergeFragment).show(fragmentManager)
		}
		b_import.setOnClickListener {
			NotebookImportDialog.newInstance(this).show(fragmentManager)
		}
	}
	
	override fun onNotebookCreated(notebookKey: NotebookKey) {
		fragmentManager.popBackStack()
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager, true)
	}
	
	override fun onNotebookImported(notebookKey: NotebookKey) {
		fragmentManager.popBackStack()
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager, true)
	}
	
}