package com.zkl.memruss.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.memruss.R
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.base.isLearning
import kotlinx.android.synthetic.main.fragment_note_view.*


class NoteViewFragment : NoteHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long)
			= NoteViewFragment::class.java.newInstance(notebookKey, noteId)
	}
	
	//view
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_view, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		//刷新缓存的词条
		if (tryLoadNote() == null) {
			fragmentManager.popBackStack()
			return
		}
		
		tv_title.text = getString(R.string.Note_view_id, noteId)
		
		if (note.isLearning) {
			val relativeReviewTime = (note.memoryState.reviewTime - System.currentTimeMillis()).toDouble() / (3600 * 1000)
			tv_info.text = getString(R.string.Note_view_info,
				note.memoryState.progress.toInt(), note.memoryState.load.toInt(), relativeReviewTime)
				.replace("\\n", "\n")
		} else {
			tv_info.visibility = View.GONE
		}
		
		b_edit.isEnabled = notebookKey.mutable
		b_edit.setOnClickListener {
			NoteEditFragment.newInstance(notebookKey, noteId).jumpFade(fragmentManager)
		}
		
		noteContentViewHolder = null
		updateNoteContent()
	}
	
	//noteContent
	private var noteContentViewHolder: NoteContentViewHolder? = null
	
	private fun updateNoteContent() {
		val noteContent = note.content
		val oldHolder = noteContentViewHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
		} else {
			val holder = noteContent.newViewHolderOrThrow(context, fl_noteContent)
			fl_noteContent.removeAllViews()
			fl_noteContent.addView(holder.view)
			noteContentViewHolder = holder
		}
	}
	
	
}


