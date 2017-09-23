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

class NotebookCreationFragment: Fragment() {
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_notebook_creation,container,false).also { rootView->
			
			val et_newBookName = rootView.findViewById(R.id.et_newBookName) as EditText
			val b_ok = rootView.findViewById(R.id.b_ok) as Button
			val b_cancel = rootView.findViewById(R.id.b_cancel) as Button
			
			b_ok.setOnClickListener {
				val newBook = myApp.noteManager.createNotebook(et_newBookName.text.toString())
				notebookActivity.jumpToFragment(NotebookFragment(myApp.noteManager.registerNotebook(newBook)), false)
			}
			b_cancel.setOnClickListener {
				notebookActivity.jumpBackFragment()
			}
			
		}
	}
}