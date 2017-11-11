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


class NoteReviewFragment : NoteHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long=-1L)
			= NoteReviewFragment::class.java.newInstance(notebookKey, noteId)
	}
	
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
		
		if (noteId == -1L) jumpToNextNote()
		
		tv_title.text = getString(R.string.Note_review_progress, note.memoryState.progress.toInt())
		b_view.setOnClickListener {
			val fragment = NoteViewFragment.newInstance(notebookKey, noteId)
			fragmentManager.jumpTo(fragment, true)
		}
		
		updateNoteContent()
	}
	
	private fun onResult(result: ReviewResult){
		
		//apply changes of memory progress
		val newMemory = result.updateNoteMemory(note.memoryState)
		mutableNotebook.modifyNoteMemory(noteId,newMemory)
		
		//jump to next note or finish page
		jumpToNextNote()
	}
	private fun jumpToNextNote(){
		val nextNote = notebook.selectNeedReviewNotes(System.currentTimeMillis()).firstOrNull()
		if (nextNote != null) {
			this.noteId = nextNote.id
			updateNoteContent()
		}else{
			fragmentManager.popBackStack()
			fragmentManager.jumpTo(NoteReviewFinishedFragment.newInstance(notebookKey),true)
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


