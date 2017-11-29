package com.zkl.zklRussian.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookShelf
import org.jetbrains.anko.bundleOf

class NotebookMenuDialog : DialogFragment() {
	
	companion object {
		val arg_notebookSummary = "notebookSummary"
		fun newInstance(notebookSummary: NotebookShelf.NotebookSummary): NotebookMenuDialog
			= NotebookMenuDialog::class.java.newInstance().apply {
			arguments += bundleOf(arg_notebookSummary to notebookSummary)
		}
	}
	
	val notebookSummary get() = arguments.getSerializable(arg_notebookSummary) as NotebookShelf.NotebookSummary
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		
		val itemPairs = arrayOf(
			getString(R.string.export_Notebook) to {
				NotebookExportFragment.newInstance(notebookSummary).jump(fragmentManager, true)
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