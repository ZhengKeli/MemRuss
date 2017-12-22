package com.zkl.zklRussian.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import com.zkl.zklRussian.R.string
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookKey

class NotebookUpgradeDialog : NotebookHoldingDialog(),
	NotebookMergingDialog.MergeCompletedListener {
	
	interface NotebookUpgradedListener {
		fun onNotebookUpgraded(notebookKey: NotebookKey, upgraded: Boolean)
	}
	
	companion object {
		fun <T> newInstance(notebookKey: NotebookKey, upgradedListener: T?): NotebookUpgradeDialog
			where T : NotebookUpgradedListener, T : Fragment
			= NotebookUpgradeDialog::class.java.newInstance(notebookKey).apply {
			setTargetFragment(upgradedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(context)
			.setTitle(string.notebook_upgrade_ConfirmTitle)
			.setMessage(string.notebook_upgrade_ConfirmMessage)
			.setNegativeButton(string.cancel) { _, _ ->
				(targetFragment as? NotebookUpgradedListener)?.onNotebookUpgraded(notebookKey,false)
			}
			.setPositiveButton(string.ok) { _, _ ->
				val (mainBodyKey,_) = myApp.notebookShelf.createNotebook(notebook.name)
				val request = NotebookMergingDialog.MergeRequest(
					mainBodyKey, notebookKey, true, true)
				NotebookMergingDialog.newInstance(request, this).show(fragmentManager)
			}
			.create()
	}
	
	override fun onMergeCompleted(notebookKey: NotebookKey) {
		(targetFragment as? NotebookUpgradedListener)?.onNotebookUpgraded(notebookKey, true)
	}
	
	override fun onCancel(dialog: DialogInterface?) {
		super.onCancel(dialog)
		(targetFragment as? NotebookUpgradedListener)?.onNotebookUpgraded(notebookKey,false)
	}
	
}


