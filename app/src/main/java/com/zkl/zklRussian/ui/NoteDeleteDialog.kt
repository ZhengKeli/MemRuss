package com.zkl.zklRussian.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.zkl.zklRussian.R

class NoteDeleteDialog:NoteHoldingDialog(){
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(context)
			.setTitle(R.string.confirmDeletion_title)
			.setMessage(R.string.confirmDeletion_message)
			.setNegativeButton(android.R.string.cancel,null)
			.setPositiveButton(android.R.string.ok,{ _, _ -> mutableNotebook.deleteNote(noteId) })
			.create()
	}
	
}
