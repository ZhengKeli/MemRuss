package com.zkl.zklRussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey

class NoteReviewFragment: NoteHoldingFragment {
	constructor():super()
	constructor(notebookKey: NotebookKey, noteId: Long) : super(notebookKey,noteId)
	
	//todo
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_review, container, false).apply {
		
	}
	
	
	
	
}


