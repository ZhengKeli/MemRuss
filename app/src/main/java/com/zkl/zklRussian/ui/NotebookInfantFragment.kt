package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.zkl.zklRussian.R

class NotebookInfantFragment : Fragment() {
	
	lateinit var b_newNoteBook: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook_infant, container, false).apply {
		b_newNoteBook = findViewById(R.id.b_newNoteBook)
	}.apply {
		b_newNoteBook.setOnClickListener {
			NotebookCreationFragment().jump(fragmentManager, true)
		}
	}
	
}