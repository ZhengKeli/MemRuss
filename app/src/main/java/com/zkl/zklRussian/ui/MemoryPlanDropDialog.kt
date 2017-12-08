package com.zkl.zklRussian.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.Fragment
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey

class MemoryPlanDropDialog : NotebookHoldingDialog() {
	
	interface MemoryPlanDroppedListener {
		fun onMemoryPlanDropped()
	}
	
	companion object {
		fun <T> newInstance(notebookKey: NotebookKey, deletedListener: T?): MemoryPlanDropDialog
			where T : MemoryPlanDroppedListener, T : Fragment
			= MemoryPlanDropDialog::class.java.newInstance(notebookKey).apply {
			setTargetFragment(deletedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(context)
			.setTitle(R.string.drop_memory_plan_ConfirmTitle)
			.setMessage(R.string.drop_memory_plan_ConfirmMessage)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(R.string.ok) { _, _ ->
				mutableNotebook.memoryPlan = null
				(targetFragment as? MemoryPlanDroppedListener)?.onMemoryPlanDropped()
			}
			.create()
	}
	
}


