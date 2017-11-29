package com.zkl.zklRussian.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey

class NoteMenuDialog : NoteHoldingDialog() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long)
			= NoteMenuDialog::class.java.newInstance(notebookKey, noteId)
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		
		val itemPairs = arrayOf(
			getString(R.string.view) to {
				NoteViewFragment.newInstance(notebookKey, noteId).jump(fragmentManager, true)
			},
			getString(R.string.edit) to {
				NoteEditFragment.newInstance(notebookKey, noteId).jump(fragmentManager, true)
			},
			getString(R.string.delete) to {
				NoteDeleteDialog.newInstance(notebookKey, noteId, false).show(fragmentManager, null)
			}
		)
		val itemNames = itemPairs.map { it.first }.toTypedArray<String>()
		val itemOperations = itemPairs.map { it.second }.toTypedArray<() -> Unit>()
		
		return AlertDialog.Builder(activity)
			.setItems(itemNames) { _, which ->
				itemOperations[which].invoke()
			}
			.create()
	}
}