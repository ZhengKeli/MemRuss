package com.zkl.zklRussian.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.zkl.zklRussian.R

class NoteMenuDialog: NoteHoldingDialog() {
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		
		val operations = arrayOf(
			getString(R.string.view) to {
				NoteViewFragment.newInstance(notebookKey, noteId).jump(fragmentManager, true)
			},
			getString(R.string.delete) to {
				NoteDeleteDialog.newInstance(notebookKey, noteId, false).show(fragmentManager, null)
			}
			
		)
		
		return AlertDialog.Builder(activity)
			.setItems(arrayOf("1","2","3")){ _, which ->
			
			}
			.create()
	}
}