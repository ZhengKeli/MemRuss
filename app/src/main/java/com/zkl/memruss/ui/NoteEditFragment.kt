package com.zkl.memruss.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.memruss.R
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.ConflictSolution
import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.base.NoteMemoryState
import com.zkl.memruss.core.note.base.isLearning
import com.zkl.memruss.core.note.modifyNoteContent
import kotlinx.android.synthetic.main.fragment_note_edit.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.ArrayBlockingQueue

class NoteEditFragment : NoteHoldingFragment(),
	NoteDeleteDialog.NoteDeletedListener,
	NoteConflictDialog.DialogResultedListener {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long)
			= NoteEditFragment::class.java.newInstance(notebookKey, noteId)
	}
	
	//view
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_edit, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		if (tryLoadNote() == null) {
			fragmentManager.popBackStack()
			return
		}
		
		tv_title.text = getString(R.string.Note_edit_id, noteId)
		b_delete.setOnClickListener {
			NoteDeleteDialog.newInstance(notebookKey, noteId,this).show(fragmentManager)
		}
		
		noteContentEditHolder = null
		updateNoteContent()
		
		if (note.isLearning) cb_resetProgress.visibility = View.VISIBLE
		else cb_resetProgress.run { visibility = View.GONE; isChecked = false }
		
		b_ok.setOnClickListener {
			val content = noteContentEditHolder?.applyChange() ?: return@setOnClickListener
			launch(CommonPool) {
				var canceled = false
				mutableNotebook.modifyNoteContent(noteId, content) { conflictNoteId, newContent ->
					val situation = NoteConflictDialog.ConflictSituation(false, conflictNoteId, newContent,
						note.isLearning && !cb_resetProgress.isChecked)
					launch(UI) {
						NoteConflictDialog.newInstance(notebookKey, situation,
							true, this@NoteEditFragment).show(fragmentManager)
					}
					val result = conflictDialogResultChan.take()
					canceled = result.canceled
					result.solution ?: ConflictSolution(false, false)
				}
				if (!canceled) {
					if (cb_resetProgress.isChecked)
						mutableNotebook.modifyNoteMemory(noteId, NoteMemoryState.infantState())
					launch(UI) { fragmentManager.popBackStack() }
				}
			}
		}
		b_cancel.setOnClickListener {
			fragmentManager.popBackStack()
		}
	}
	
	override fun onResume() {
		super.onResume()
		noteContentEditHolder?.requestFocus()
	}
	
	override fun onNoteDeleted() {
		fragmentManager.popBackStack()
	}
	
	private val conflictDialogResultChan = ArrayBlockingQueue<NoteConflictDialog.DialogResult>(1)
	override fun onDialogResulted(result: NoteConflictDialog.DialogResult) {
		conflictDialogResultChan.put(result)
	}
	
	//noteContent
	private var noteContentEditHolder: NoteContentEditHolder? = null
	private fun updateNoteContent(noteContent: NoteContent = note.content) {
		val oldHolder = noteContentEditHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
		} else {
			val holder = noteContent.newEditHolderOrThrow(context,fl_noteContent)
			fl_noteContent.removeAllViews()
			fl_noteContent.addView(holder.view)
			noteContentEditHolder = holder
		}
	}
	
}

