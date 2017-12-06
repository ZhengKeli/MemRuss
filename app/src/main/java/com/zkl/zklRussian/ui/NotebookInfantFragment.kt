package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import org.jetbrains.anko.find

class NotebookInfantFragment : Fragment(),NotebookCreationDialog.NotebookCreatedListener {
	
	lateinit var b_newNoteBook: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook_infant, container, false).apply {
		b_newNoteBook = find(R.id.b_newNoteBook)
	}.apply {
		b_newNoteBook.setOnClickListener {
			NotebookCreationDialog.newInstance(this@NotebookInfantFragment).show(fragmentManager)
		}
	}
	
	override fun onNotebookCreated(notebookKey: NotebookKey) {
		fragmentManager.popBackStack()
		NotebookFragment.newInstance(notebookKey).jump(fragmentManager, true)
	}
	
}