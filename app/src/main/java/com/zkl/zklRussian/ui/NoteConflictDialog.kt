package com.zkl.zklRussian.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ListView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.Note
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.NoteMemoryState
import com.zkl.zklRussian.core.note.NoteMemoryStatus
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.find
import java.io.Serializable


class NoteConflictDialog : NotebookHoldingDialog() {
	
	data class ModifyRequest(val targetNoteId: Long, val noteContent: NoteContent, val remainProgress: Boolean) : Serializable
	companion object {
		val arg_modifyRequest = "modifyRequest"
		fun newInstance(notebookKey: NotebookKey, modifyRequest: ModifyRequest)
			= NoteConflictDialog::class.java.newInstance(notebookKey).apply {
			arguments += bundleOf(
				arg_modifyRequest to modifyRequest)
		}
		
		val NoteConflictDialog.modifyRequest get() = arguments.getSerializable(arg_modifyRequest) as ModifyRequest
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
		val dialog = AlertDialog.Builder(context).setTitle(R.string.there_are_conflicted_notes).create()
		val view = View.inflate(context, R.layout.fragment_note_conflict, null).apply {
			
			//prepare data
			val request = modifyRequest
			val conflictNotes = getConflictNotes(request.targetNoteId, request.noteContent)
			
			//find views
			val lv_conflict:ListView = find(R.id.lv_conflict)
			val cb_remainProgress:CheckBox = find(R.id.cb_remainProgress)
			
			//prepare views
			dialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.cancel)) { _, _ -> }
			lv_conflict.adapter = object : NoteListAdapter() {
				override fun getCount(): Int = conflictNotes.size
				override fun getItem(position: Int): Note = conflictNotes[position]
				override val context: Context get() = activity
			}
			if (conflictNotes.size == 1) {
				val conflictNote = conflictNotes.first()
				if (request.targetNoteId == -1L) {
					// create
					if (conflictNote.memoryState.status != NoteMemoryStatus.infant)
						cb_remainProgress.visibility = View.VISIBLE
					
					dialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.override)) { _, _ ->
						mutableNotebook.modifyNoteContent(conflictNote.id, request.noteContent)
						if (!cb_remainProgress.isChecked)
							mutableNotebook.modifyNoteMemory(conflictNote.id, NoteMemoryState.infantState())
						fragmentManager.popBackStack()
					}
					
				} else {
					//modify
					dialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.override)) { _, _ ->
						mutableNotebook.deleteNote(conflictNote.id)
						mutableNotebook.modifyNoteContent(request.targetNoteId, request.noteContent)
						if (!request.remainProgress)
							mutableNotebook.modifyNoteMemory(request.targetNoteId, NoteMemoryState.infantState())
						fragmentManager.popBackStack()
					}
				}
			}
			
		}
		dialog.setView(view)
		return dialog
	}
	
	private fun getConflictNotes(targetNoteId: Long, noteContent: NoteContent)
		= noteContent.uniqueTags.mapNotNull { uniqueTag ->
		val noteId = notebook.checkUniqueTag(uniqueTag, targetNoteId)
		if (noteId != -1L) notebook.getNote(noteId) else null
	}
	
}

