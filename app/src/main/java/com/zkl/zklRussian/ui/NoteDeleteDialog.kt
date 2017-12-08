package com.zkl.zklRussian.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.Fragment
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey

class NoteDeleteDialog : NoteHoldingDialog() {
	
	interface NoteDeletedListener {
		fun onNoteDeleted()
	}
	
	companion object {
		fun <T> newInstance(notebookKey: NotebookKey, noteId: Long, deletedListener: T?)
			where T : NoteDeletedListener, T : Fragment
			= NoteDeleteDialog::class.java.newInstance(notebookKey, noteId).apply {
			setTargetFragment(deletedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(context)
			.setTitle(R.string.note_deletion_ConfirmTitle)
			.setMessage(R.string.note_deletion_ConfirmMessage)
			.setNegativeButton(R.string.cancel, null)
			.setPositiveButton(R.string.ok) { _, _ ->
				mutableNotebook.deleteNote(noteId)
				(targetFragment as? NoteDeletedListener)?.onNoteDeleted()
			}
			.create()
	}
	
}


