package com.zkl.memruss.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.MutableNotebook

class NoteDeleteDialog : DialogFragment() {
	
	interface NoteDeletedListener {
		fun onNoteDeleted()
	}
	
	companion object {
		fun <T> newInstance(notebookKey: NotebookKey, noteId: Long, deletedListener: T?): NoteDeleteDialog
			where T : NoteDeletedListener, T : Fragment {
			return NoteDeleteDialog::class.java.newInstance(notebookKey, noteId).apply {
				setTargetFragment(deletedListener, 0)
			}
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(context)
			.setTitle(R.string.note_delete_ConfirmTitle)
			.setMessage(R.string.note_delete_ConfirmMessage)
			.setNegativeButton(R.string.cancel, null)
			.setPositiveButton(R.string.ok) { _, _ ->
				(myApp.notebookShelf.restoreNotebook(argNotebookKey) as MutableNotebook).deleteNote(argNoteId)
				(targetFragment as? NoteDeletedListener)?.onNoteDeleted()
			}
			.create()
	}
	
}


