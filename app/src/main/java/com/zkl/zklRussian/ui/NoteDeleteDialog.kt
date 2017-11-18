package com.zkl.zklRussian.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import org.jetbrains.anko.bundleOf

class NoteDeleteDialog:NoteHoldingDialog(){
	
	companion object {
		private val arg_popBackStack = "popBackStack"
		fun newInstance(notebookKey: NotebookKey, noteId: Long,popBackStack: Boolean=true)
			= NoteDeleteDialog::class.java.newInstance(notebookKey, noteId).apply {
			arguments+= bundleOf(arg_popBackStack to popBackStack)
		}
	}
	
	val popBackStack:Boolean get() = arguments.getBoolean(arg_popBackStack)
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(context)
			.setTitle(R.string.confirm_deletion)
			.setMessage(R.string.confirm_deletion_message)
			.setNegativeButton(android.R.string.cancel,null)
			.setPositiveButton(android.R.string.ok,{ _, _ ->
				mutableNotebook.deleteNote(noteId)
				if(popBackStack) fragmentManager.popBackStack()
			})
			.create()
	}
	
}
