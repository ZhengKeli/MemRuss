package com.zkl.zklRussian.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookBrief
import org.jetbrains.anko.bundleOf

class NotebookMenuDialog : DialogFragment(),NotebookDeleteDialog.NotebookDeletedListener {
	
	interface NotebookListChangedListener {
		fun onNotebookListChanged()
	}
	
	companion object {
		val arg_notebookBrief = "notebookBrief"
		fun <T>newInstance(notebookBrief: NotebookBrief, changedListener: T?): NotebookMenuDialog
			where T : NotebookListChangedListener, T : Fragment
			= NotebookMenuDialog::class.java.newInstance().apply {
			arguments += bundleOf(arg_notebookBrief to notebookBrief)
			setTargetFragment(changedListener, 0)
		}
	}
	
	private val notebookBrief get() = arguments.getSerializable(arg_notebookBrief) as NotebookBrief
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		
		val itemPairs = arrayOf(
			getString(R.string.export_Notebook) to {
				NotebookExportDialog.newInstance(notebookBrief).show(fragmentManager)
			},
			getString(R.string.delete_Notebook) to {
				NotebookDeleteDialog.newInstance(notebookBrief,this).show(fragmentManager)
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
	
	override fun onNotebookDeleted() {
		(targetFragment as? NotebookListChangedListener)?.onNotebookListChanged()
	}
	
}