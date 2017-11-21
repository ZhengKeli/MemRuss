package com.zkl.zklRussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
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
	private lateinit var fl_noteContent:FrameLayout
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_review, container, false).apply {
		tv_title = findViewById(R.id.tv_title) as TextView
		b_view = findViewById(R.id.b_view) as Button
		fl_noteContent = findViewById(R.id.fl_noteContent) as FrameLayout
		
		noteContentReviewHolder = null
		
		if (tryLoadNote() == null) jumpToNextNote()
		
		tv_title.text = getString(R.string.Note_review_progress, note.memoryState.progress.toInt())
		b_view.setOnClickListener {
			NoteViewFragment.newInstance(notebookKey, noteId).jump(fragmentManager, true)
		}
		
		updateNoteContent()
	}
	
	private fun onResult(result: ReviewResult){
		
		//apply changes of memory progress
		val newMemory = result.updateNoteMemory(note.memoryState)
		mutableNotebook.modifyNoteMemory(noteId,newMemory)
		mutableNotebook.fillNotesByPlan()
		
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
			NoteReviewFinishedFragment.newInstance(notebookKey).jump(fragmentManager, true)
		}
	}
	
	
	//noteContent
	private var noteContentReviewHolder:NoteContentReviewHolder? = null
	private fun updateNoteContent(){
		val noteContent = note.content
		val oldHolder = noteContentReviewHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
		} else {
			val holder = typedNoteContentReviewHolders[noteContent.typeTag]?.invoke(context,fl_noteContent)
				?: throw RuntimeException("The noteContent type \"${noteContent.typeTag}\" is not supported.")
			holder.noteContent = noteContent
			holder.onResultListener = this::onResult
			fl_noteContent.removeAllViews()
			fl_noteContent.addView(holder.view)
			noteContentReviewHolder = holder
		}
	}
	
}


