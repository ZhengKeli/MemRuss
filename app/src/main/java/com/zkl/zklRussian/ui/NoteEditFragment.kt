package com.zkl.zklRussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.*
import com.zkl.zklRussian.core.note.base.NoteMemoryState
import kotlinx.android.synthetic.main.fragment_note_edit.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.ArrayBlockingQueue

class NoteEditFragment : NoteHoldingFragment(),
	NoteDeleteDialog.NoteDeletedListener,
	NoteConflictDialog.ConflictSolvedListener {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long)
			= NoteEditFragment::class.java.newInstance(notebookKey, noteId)
	}
	
	//view
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_edit, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		if (noteId == -1L) initViewsCreateMode()
		else initViewsEditMode()
	}
	
	private fun initViewsEditMode() {
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
		cb_resetProgress.visibility = View.VISIBLE
		
		b_ok.setOnClickListener {
			val content = noteContentEditHolder!!.applyChange()
			launch(CommonPool) {
				var committed = true
				mutableNotebook.modifyNoteContent(noteId, content) { newContent, conflictNoteId ->
					val modifyRequest = NoteConflictDialog.ModifyRequest(newContent, noteId, conflictNoteId)
					launch(UI){
						NoteConflictDialog.newInstance(notebookKey, modifyRequest,
							true, this@NoteEditFragment).show(fragmentManager)
					}
					val solution = conflictSolutionChan.take()
					if (solution == null) committed = false
					solution ?: ConflictSolution(false, false)
				}
				if (committed) {
					if (cb_resetProgress.isChecked)
						mutableNotebook.modifyNoteMemory(noteId, NoteMemoryState.infantState())
					fragmentManager.popBackStack()
				}
			}
		}
		b_cancel.setOnClickListener {
			fragmentManager.popBackStack()
		}
	}
	
	private fun initViewsCreateMode() {
		tv_title.text = getString(R.string.Note_create)
		b_delete.visibility = View.GONE
		
		noteContentEditHolder = null
		updateNoteContent(QuestionContent("", ""))
		cb_resetProgress.visibility = View.GONE
		
		b_ok.setOnClickListener {
			val content = noteContentEditHolder!!.applyChange()
			launch(CommonPool){
				var committed = true
				mutableNotebook.addNote(content) { newContent, conflictNoteId ->
					val modifyRequest = NoteConflictDialog.ModifyRequest(newContent, -1, conflictNoteId)
					launch(UI){
						NoteConflictDialog.newInstance(notebookKey, modifyRequest,
							true, this@NoteEditFragment).show(fragmentManager)
					}
					val solution = conflictSolutionChan.take()
					if (solution == null) committed = false
					solution ?: ConflictSolution(false, false)
				}
				if(committed) fragmentManager.popBackStack()
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
	
	private val conflictSolutionChan = ArrayBlockingQueue<ConflictSolution?>(1)
	override fun onConflictSolved(solution: ConflictSolution?) {
		conflictSolutionChan.put(solution)
	}
	
	//noteContent
	private var noteContentEditHolder: NoteContentEditHolder? = null
	private fun updateNoteContent(noteContent: NoteContent = note.content) {
		val oldHolder = noteContentEditHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
		} else {
			val holder = typedNoteContentEditHolders[noteContent.typeTag]?.invoke(activity, fl_noteContent)
				?: throw RuntimeException("The noteContent type \"${noteContent.typeTag}\" is not supported.")
			holder.noteContent = noteContent
			fl_noteContent.removeAllViews()
			fl_noteContent.addView(holder.view)
			noteContentEditHolder = holder
		}
	}
	
}

