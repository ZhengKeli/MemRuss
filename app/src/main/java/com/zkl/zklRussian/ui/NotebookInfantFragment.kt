package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.zkl.zklRussian.R

class NotebookInfantFragment: Fragment() {
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
		= inflater.inflate(R.layout.fragment_notebook_infant,container,false)?.also { ll_root->
		
		val b_newNoteBook = ll_root.findViewById(R.id.b_newNoteBook)
		b_newNoteBook.setOnClickListener {
			notebookActivity.jumpToFragment(NotebookCreationFragment(),true)
		}
		
	}
}