package com.zkl.memruss.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.View
import com.zkl.memruss.R
import com.zkl.memruss.control.note.NotebookKey
import kotlinx.android.synthetic.main.dialog_notebook_rename.view.*

class NotebookRenameDialog : NotebookHoldingDialog() {
	
	interface NotebookRenamedListener {
		fun onNotebookRenamed(notebookKey: NotebookKey)
	}
	
	companion object {
		fun <T> newInstance(notebookKey: NotebookKey, onCreatedListener: T?): NotebookRenameDialog
			where T : NotebookRenamedListener, T : Fragment
			= NotebookRenameDialog::class.java.newInstance(notebookKey).apply {
			setTargetFragment(onCreatedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val view = View.inflate(context, R.layout.dialog_notebook_rename, null)
		view.et_newName.setText(notebook.name)
		return AlertDialog.Builder(context)
			.setTitle(R.string.notebook_new_name)
			.setView(view)
			.setPositiveButton(R.string.ok) { _, _ ->
				mutableNotebook.name = view.et_newName.text.toString()
				(targetFragment as? NotebookRenamedListener)?.onNotebookRenamed(notebookKey)
			}
			.setNegativeButton(R.string.cancel, null)
			.create()
	}
	
}