package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.zkl.zklRussian.R


class NoteReviewFragment : NoteHoldingFragment() {
	
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
			mainActivity.jumpToFragment(NoteViewFragment().also {
				it.notebookKey=notebookKey
				it.noteId=-1
			}, true)
		}
		
		updateNoteContent()
	}
	
	private fun onResult(result: ReviewResult){
		
		//apply changes of memory progress
		val newMemory = result.updateNoteMemory(note.memory)
		mutableNotebook.modifyNoteMemory(noteId,newMemory)
		
		//jump to next note or finish page
		val nextNote = notebook.selectNeedReviewNotes(System.currentTimeMillis()).firstOrNull()
		if (nextNote != null) {
			this.noteId = nextNote.id
			updateNoteContent()
		}else{
			//todo 显示结束画面
		}
	}
	
	
	//noteContent
	private var noteContentReviewFragment: NoteContentReviewFragment? = null
	override fun onAttachFragment(childFragment: Fragment) {
		super.onAttachFragment(childFragment)
		noteContentReviewFragment = childFragment as? NoteContentReviewFragment
	}
	private fun updateNoteContent(){
		val noteContent = note.content
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


