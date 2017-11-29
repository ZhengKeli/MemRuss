package com.zkl.zklRussian.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey


class NoteReviewFinishedFragment : NotebookHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= NoteReviewFinishedFragment::class.java.newInstance(notebookKey)
	}
	
	lateinit var b_back: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_review_finished, container, false).apply {
		b_back = findViewById(R.id.b_back)
	}.apply {
		b_back.setOnClickListener {
			fragmentManager.popBackStack()
		}
	}
	
}
