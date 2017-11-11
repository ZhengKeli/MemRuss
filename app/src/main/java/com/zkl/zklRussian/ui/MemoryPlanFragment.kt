package com.zkl.zklRussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey

class MemoryPlanFragment :NotebookHoldingFragment(){
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
		= MemoryPlanFragment::class.java.newInstance(notebookKey)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_memory_plan, container, false)
	
	override fun onStart() {
		super.onStart()
		updateViews()
	}
	
	fun updateViews(){
		val memory = notebook.memory
		
	}
}
