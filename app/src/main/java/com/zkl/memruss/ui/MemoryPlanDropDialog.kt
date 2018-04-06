package com.zkl.memruss.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.MutableNotebook

class MemoryPlanDropDialog : DialogFragment() {
	
	interface MemoryPlanDroppedListener {
		fun onMemoryPlanDropped()
	}
	
	companion object {
		fun <T> newInstance(notebookKey: NotebookKey, deletedListener: T?): MemoryPlanDropDialog
			where T : MemoryPlanDroppedListener, T : Fragment = MemoryPlanDropDialog::class.java.newInstance(notebookKey).apply {
			setTargetFragment(deletedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(context)
			.setTitle(R.string.drop_memory_plan_ConfirmTitle)
			.setMessage(R.string.drop_memory_plan_ConfirmMessage)
			.setNegativeButton(R.string.cancel, null)
			.setPositiveButton(R.string.ok) { _, _ ->
				val mutableNotebook = myApp.notebookShelf.restoreNotebook(argNotebookKey) as MutableNotebook
				mutableNotebook.memoryPlan = null
				(targetFragment as? MemoryPlanDroppedListener)?.onMemoryPlanDropped()
			}
			.create()
	}
	
}
