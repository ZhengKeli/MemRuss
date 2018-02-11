package com.zkl.memruss.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.memruss.R
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.base.getNextNeedReviewNote
import kotlinx.android.synthetic.main.fragment_note_review.*


class NoteReviewFragment : NoteHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long = -1L) = NoteReviewFragment::class.java.newInstance(notebookKey, noteId)
	}
	
	//view
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_note_review, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		noteContentReviewHolder = null
		if (tryLoadNote() == null) jumpToNextNote()
		else updateNoteContent()
		
		b_view.setOnClickListener {
			NoteViewFragment.newInstance(notebookKey, noteId).jumpFade(fragmentManager)
		}
		
	}
	
	private fun onResult(result: ReviewResult) {
		
		//apply changes of memory progress
		val newMemory = result.updateNoteMemory(note.memoryState)
		mutableNotebook.modifyNoteMemory(noteId, newMemory)
		mutableNotebook.activateNotesByPlan()
		
		//jump to next note or finish page
		jumpToNextNote()
	}
	
	private var noteContentReviewHolder: NoteContentReviewHolder? = null
	private fun updateNoteContent() {
		val remainCount = notebook.countNeedReviewNotes(System.currentTimeMillis())
		tv_title.text = getString(R.string.Note_review_remainCount, remainCount)
		
		val noteContent = note.content
		val oldHolder = noteContentReviewHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
		} else {
			val holder = noteContent.newReviewHolderOrThrow(context, fl_noteContent)
			holder.onResultListener = this::onResult
			fl_noteContent.removeAllViews()
			fl_noteContent.addView(holder.view)
			noteContentReviewHolder = holder
		}
	}
	
	private fun jumpToNextNote() {
		val nextNote = notebook.getNextNeedReviewNote()
		if (nextNote != null) {
			this.noteId = nextNote.id
			updateNoteContent()
		} else {
			fragmentManager.popBackStack()
			NoteReviewFinishedFragment.newInstance(notebookKey).jump(fragmentManager)
		}
	}
	
}


