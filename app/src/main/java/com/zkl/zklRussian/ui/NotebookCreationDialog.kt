package com.zkl.zklRussian.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.View
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookKey
import kotlinx.android.synthetic.main.dialog_notebook_creation.view.*

class NotebookCreationDialog : DialogFragment() {
	
	interface NotebookCreatedListener {
		fun onNotebookCreated(notebookKey: NotebookKey)
	}
	
	companion object {
		fun <T> newInstance(onCreatedListener: T?): NotebookCreationDialog
			where T : NotebookCreatedListener, T : Fragment
			= NotebookCreationDialog::class.java.newInstance().apply {
			setTargetFragment(onCreatedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val view = View.inflate(context, R.layout.dialog_notebook_creation, null)
		return AlertDialog.Builder(context)
			.setTitle(R.string.new_notebook_name)
			.setView(view)
			.setPositiveButton(R.string.ok) { _, _ ->
				val (key, _) = myApp.notebookShelf.createNotebook(view.et_newBookName.text.toString())
				(targetFragment as? NotebookCreatedListener)?.onNotebookCreated(key)
			}
			.setNegativeButton(android.R.string.cancel, null)
			.create()
	}
	
}