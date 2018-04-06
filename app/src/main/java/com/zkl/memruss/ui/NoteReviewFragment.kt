package com.zkl.memruss.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.MutableNotebook
import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.base.getNextNeedReviewNote
import com.zkl.memruss.core.note.base.getNoteOrNull
import kotlinx.android.synthetic.main.fragment_note_review.*


class NoteReviewFragment : Fragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long = -1L): NoteReviewFragment {
			return NoteReviewFragment::class.java.newInstance(notebookKey, noteId)
		}
	}
	
	//view
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.fragment_note_review, container, false)
	}
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		b_view.setOnClickListener {
			NoteViewFragment.newInstance(argNotebookKey, argNoteId).jumpFade(fragmentManager)
		}
		
		updateViews()
		
	}
	
	private fun updateViews() {
		//read note
		val notebookKey = argNotebookKey
		val noteId = argNoteId
		val mutableNotebook = myApp.notebookShelf.restoreNotebook(notebookKey) as MutableNotebook
		val note = mutableNotebook.getNoteOrNull(argNoteId) ?: kotlin.run { jumpToNextNote();return }
		
		val remainCount = mutableNotebook.countNeedReviewNotes(System.currentTimeMillis())
		tv_title.text = getString(R.string.Note_review_remainCount, remainCount)
		
		updateNoteContent(note.content) { result ->
			//apply changes of memory progress
			val newMemory = result.updateNoteMemory(note.memoryState)
			mutableNotebook.modifyNoteMemory(noteId, newMemory)
			mutableNotebook.activateNotesByPlan()
			
			//jump to next note or finish page
			jumpToNextNote()
		}
		
	}
	
	private var noteContentReviewHolder: NoteContentReviewHolder? = null
	private fun updateNoteContent(noteContent: NoteContent, onResult: ((ReviewResult) -> Unit)?) {
		val oldHolder = noteContentReviewHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
			oldHolder.onResultListener = onResult
		} else {
			val holder = noteContent.newReviewHolderOrThrow(context, fl_noteContent)
			holder.onResultListener = onResult
			fl_noteContent.removeAllViews()
			fl_noteContent.addView(holder.view)
			noteContentReviewHolder = holder
		}
	}
	
	private fun jumpToNextNote() {
		val notebookKey = argNotebookKey
		val mutableNotebook = myApp.notebookShelf.restoreNotebook(notebookKey) as MutableNotebook
		val nextNote = mutableNotebook.getNextNeedReviewNote()
		if (nextNote != null) {
			this.argNoteId = nextNote.id
			updateViews()
		} else {
			fragmentManager.popBackStack()
			NoteReviewFinishedFragment.newInstance().jump(fragmentManager)
		}
	}
}


