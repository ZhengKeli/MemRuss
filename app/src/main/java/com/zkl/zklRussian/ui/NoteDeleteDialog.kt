package com.zkl.zklRussian.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey

class NoteDeleteDialog:NoteHoldingDialog{
	
	constructor():super()
	constructor(notebookKey: NotebookKey, noteId: Long) : super(notebookKey, noteId)
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(context)
			.setTitle(R.string.confirmDeletion_title)
			.setMessage(R.string.confirmDeletion_message)
			.setNegativeButton(android.R.string.cancel,null)
			.setPositiveButton(android.R.string.ok,{ _, _ -> mutableNotebook.deleteNote(noteId) })
			.create()
	}
	
}
