package com.zkl.zklRussian.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey

class NoteMenuDialog : NoteHoldingDialog(),NoteDeleteDialog.DeletionConfirmedListener {
	
	interface NoteListChangedListener {
		fun onNoteListChanged()
	}
	
	companion object {
		fun <T>newInstance(notebookKey: NotebookKey, noteId: Long, changedListener: T?)
			where T : NoteListChangedListener, T : Fragment
			= NoteMenuDialog::class.java.newInstance(notebookKey, noteId).apply {
			setTargetFragment(changedListener,0)
		}
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
				NoteDeleteDialog.newInstance(notebookKey, noteId,this).show(fragmentManager, null)
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
	
	override fun onDeletionConfirmed() {
		(targetFragment as? NoteListChangedListener)?.onNoteListChanged()
	}
	
}