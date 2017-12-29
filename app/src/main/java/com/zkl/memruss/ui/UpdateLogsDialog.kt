package com.zkl.memruss.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.zkl.memruss.R
import com.zkl.memruss.control.settings.AppVersion

class UpdateLogsDialog : DialogFragment() {
	
	companion object {
		fun newInstance(): UpdateLogsDialog = UpdateLogsDialog::class.java.newInstance()
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(context)
			.setTitle(getString(R.string.update_logs))
			.setItems(AppVersion.updateLogs, null)
			.create()
	}
	
}


