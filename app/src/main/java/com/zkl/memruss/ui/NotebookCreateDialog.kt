package com.zkl.memruss.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.View
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookKey
import kotlinx.android.synthetic.main.dialog_notebook_create.view.*

class NotebookCreateDialog : DialogFragment() {
	
	interface NotebookCreatedListener {
		fun onNotebookCreated(notebookKey: NotebookKey)
	}
	
	companion object {
		fun <T> newInstance(onCreatedListener: T?): NotebookCreateDialog
			where T : NotebookCreatedListener, T : Fragment
			= NotebookCreateDialog::class.java.newInstance().apply {
			setTargetFragment(onCreatedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val view = View.inflate(context, R.layout.dialog_notebook_create, null)
		return AlertDialog.Builder(context)
			.setTitle(R.string.new_notebook_name)
			.setView(view)
			.setPositiveButton(R.string.ok) { _, _ ->
				val (key, _) = myApp.notebookShelf.createNotebook(view.et_newNotebookName.text.toString())
				(targetFragment as? NotebookCreatedListener)?.onNotebookCreated(key)
			}
			.setNegativeButton(R.string.cancel, null)
			.create()
	}
	
}