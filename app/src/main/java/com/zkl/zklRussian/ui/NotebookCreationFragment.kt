package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import org.jetbrains.anko.find

class NotebookCreationFragment : Fragment() {
	
	
	lateinit var et_newBookName: EditText
	lateinit var b_ok: Button
	lateinit var b_cancel: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook_creation, container, false).apply {
		et_newBookName = find(R.id.et_newBookName)
		b_ok = find(R.id.b_ok)
		b_cancel = find(R.id.b_cancel)
	}.apply {
		b_ok.setOnClickListener {
			val (key, _) = myApp.notebookShelf.createNotebook(et_newBookName.text.toString())
			fragmentManager.popBackStack()
			NotebookFragment.newInstance(key).jump(fragmentManager, false)
		}
		b_cancel.setOnClickListener {
			fragmentManager.popBackStack()
		}
	}
}