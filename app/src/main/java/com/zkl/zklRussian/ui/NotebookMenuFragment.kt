package com.zkl.zklRussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.zklRussian.R

class NotebookMenuFragment:NotebookHoldingFragment {
	constructor() : super()
	constructor(notebookKey: Int) : super(notebookKey)
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= inflater.inflate(R.layout.fragment_notebook, container, false).also {
		
		
		
	}
	
}