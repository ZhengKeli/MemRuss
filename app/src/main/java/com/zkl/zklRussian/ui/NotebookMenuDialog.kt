package com.zkl.zklRussian.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey

class NotebookMenuDialog : NotebookHoldingDialog() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= NotebookMenuDialog::class.java.newInstance(notebookKey)
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		
		val itemPairs = arrayOf(
			getString(R.string.export_Notebook) to {
				//todo open export dialog
			}
		)
		val itemNames = itemPairs.map { it.first }.toTypedArray<String>()
		val itemOperations = itemPairs.map { it.second }.toTypedArray<()->Unit>()
		
		return AlertDialog.Builder(activity)
			.setItems(itemNames){ _, which ->
				itemOperations[which].invoke()
			}
			.create()
	}
}