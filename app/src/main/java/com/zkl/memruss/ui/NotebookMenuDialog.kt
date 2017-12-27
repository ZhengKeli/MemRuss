package com.zkl.memruss.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookBrief
import com.zkl.memruss.control.note.NotebookKey
import org.jetbrains.anko.bundleOf

class NotebookMenuDialog : DialogFragment(),
	NotebookDeleteDialog.NotebookDeletedListener,
	NotebookRenameDialog.NotebookRenamedListener {
	
	companion object {
		val arg_notebookBrief = "notebookBrief"
		fun <T>newInstance(notebookBrief: NotebookBrief, changedListener: T?): NotebookMenuDialog
			where T : Fragment, T : NotebookDeleteDialog.NotebookDeletedListener,
			      T : NotebookRenameDialog.NotebookRenamedListener
			= NotebookMenuDialog::class.java.newInstance().apply {
			arguments += bundleOf(arg_notebookBrief to notebookBrief)
			setTargetFragment(changedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val notebookBrief = arguments[arg_notebookBrief] as NotebookBrief
		
		val itemPairs = arrayListOf(
			getString(R.string.export_Notebook) to {
				NotebookExportDialog.newInstance(notebookBrief).show(fragmentManager)
			},
			getString(R.string.delete_Notebook) to {
				NotebookDeleteDialog.newInstance(notebookBrief, this).show(fragmentManager)
			}
		)
		if (notebookBrief.mutable) itemPairs.addAll(arrayListOf(
			getString(R.string.rename_Notebook) to {
				val (key, _) = myApp.notebookShelf.openMutableNotebook(notebookBrief.file)
				NotebookRenameDialog.newInstance(key, this).show(fragmentManager)
			}
		))
		
		val itemNames = itemPairs.map { it.first }.toTypedArray<String>()
		val itemOperations = itemPairs.map { it.second }.toTypedArray<() -> Unit>()
		
		return AlertDialog.Builder(activity)
			.setItems(itemNames) { _, which ->
				itemOperations[which].invoke()
			}
			.create()
	}
	
	override fun onNotebookDeleted() {
		(targetFragment as? NotebookDeleteDialog.NotebookDeletedListener)?.onNotebookDeleted()
	}
	
	override fun onNotebookRenamed(notebookKey: NotebookKey) {
		(targetFragment as? NotebookRenameDialog.NotebookRenamedListener)?.onNotebookRenamed(notebookKey)
	}
	
}