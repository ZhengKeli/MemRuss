package com.zkl.zklRussian.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.ConflictSolution
import com.zkl.zklRussian.core.note.Note
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.base.NoteMemoryStatus
import kotlinx.android.synthetic.main.dialog_note_conflict.view.*
import org.jetbrains.anko.bundleOf
import java.io.Serializable

class NoteConflictDialog : NotebookHoldingDialog() {
	
	data class ModifyRequest(val newContent: NoteContent, val targetNoteId: Long, val conflictNoteId: Long) : Serializable
	
	interface ConflictSolvedListener {
		fun onConflictSolved(solution: ConflictSolution?)
	}
	
	companion object {
		private val arg_modifyRequest = "modifyRequest"
		private val arg_cancelable = "cancelable"
		fun <T> newInstance(notebookKey: NotebookKey, modifyRequest: ModifyRequest, cancelable: Boolean, solvedListener: T)
			where T : ConflictSolvedListener, T : Fragment
			= NoteConflictDialog::class.java.newInstance(notebookKey).apply {
			arguments += bundleOf(
				arg_modifyRequest to modifyRequest,
				arg_cancelable to cancelable
			)
			setTargetFragment(solvedListener, 0)
		}
		
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
		
		//prepare data
		val request = arguments.getSerializable(arg_modifyRequest) as ModifyRequest
		val cancelable = arguments.getBoolean(arg_cancelable, false)
		val conflictNotes = request.newContent.uniqueTags.mapNotNull { uniqueTag ->
			val noteId = notebook.checkUniqueTag(uniqueTag, request.targetNoteId)
			if (noteId != -1L) notebook.getNote(noteId) else null
		}
		
		//prepare views
		val view = View.inflate(context, R.layout.dialog_note_conflict, null)
		val dialogBuilder = AlertDialog.Builder(context).setView(view)
		
		dialogBuilder.setTitle(R.string.there_are_conflicted_notes)
		view.lv_conflict.adapter = object : NoteListAdapter() {
			override fun getCount(): Int = conflictNotes.size
			override fun getItem(position: Int): Note = conflictNotes[position]
			override val context: Context get() = activity
		}
		if (conflictNotes.size == 1) {
			val conflictNote = conflictNotes.first()
			if (request.targetNoteId == -1L && conflictNote.memoryState.status != NoteMemoryStatus.infant) {
				view.cb_resetProgress.visibility = View.VISIBLE
				view.cb_resetProgress.isChecked = false
			} else {
				view.cb_resetProgress.visibility = View.GONE
				view.cb_resetProgress.isChecked = false
			}
			
			dialogBuilder.setPositiveButton(R.string.override) { _, _ ->
				makeCallback(true, view.cb_resetProgress.isChecked)
			}
			if (cancelable) dialogBuilder.setNegativeButton(R.string.cancel) { _, _ ->
				makeNullCallback()
			}
			else dialogBuilder.setNegativeButton(R.string.remain_old) { _, _ ->
				makeCallback(false, false)
			}
		}
		
		isCancelable = cancelable
		return dialogBuilder.create()
	}
	
	override fun onCancel(dialog: DialogInterface?) {
		super.onCancel(dialog)
		makeNullCallback()
	}
	
	private fun makeCallback(override: Boolean, resetProgress: Boolean) {
		(targetFragment as? ConflictSolvedListener)
			?.onConflictSolved(ConflictSolution(override, resetProgress))
	}
	
	private fun makeNullCallback() {
		(targetFragment as? ConflictSolvedListener)
			?.onConflictSolved(null)
	}
	
}

