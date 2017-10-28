package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.NoteContent


class NoteReviewFragment : NoteHoldingFragment {
	
	constructor() : super()
	constructor(notebookKey: NotebookKey, noteId: Long) : super(notebookKey, noteId)
	
	//view
	private lateinit var tv_title: TextView
	private lateinit var b_view: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_review, container, false).apply {
		tv_title = findViewById(R.id.tv_title) as TextView
		b_view = findViewById(R.id.b_view) as Button
	}
	override fun onStart() {
		super.onStart()
		
		tv_title.text = getString(R.string.Note_review_progress, note.memory.progress.toInt())
		b_view.setOnClickListener {
			mainActivity.jumpToFragment(NoteViewFragment(notebookKey, noteId), true)
		}
		
		updateNoteContent(note.content)
	}
	
	private fun onResult(result: ReviewResult){
		//todo 跳到下一个词条
	}
	
	
	//noteContent
	private var noteContentReviewFragment: NoteContentReviewFragment? = null
	override fun onAttachFragment(childFragment: Fragment) {
		super.onAttachFragment(childFragment)
		noteContentReviewFragment = childFragment as? NoteContentReviewFragment
	}
	private fun updateNoteContent(noteContent: NoteContent){
		if (noteContentReviewFragment?.isCompatible(noteContent) == true) {
			noteContentReviewFragment?.noteContent = noteContent
		} else {
			val fragment = typedNoteContentReviewFragments[noteContent.typeTag]?.newInstance()
				?: throw RuntimeException("The noteContent type \"${noteContent.typeTag}\" is not supported.")
			childFragmentManager.beginTransaction()
				.replace(R.id.fl_noteContent_container, fragment)
				.commit()
			fragment.noteContent = noteContent
			fragment.onResultListener = this::onResult
			noteContentReviewFragment = fragment
		}
	}
	
}


