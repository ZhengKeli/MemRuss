package com.zkl.zklRussian.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.View
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookKey
import kotlinx.android.synthetic.main.dialog_notebook_import.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.toast
import java.io.File

class NotebookImportDialog : DialogFragment() {
	
	interface NotebookImportedListener{
		fun onNotebookImported(notebookKey: NotebookKey)
	}
	
	companion object {
		fun <T> newInstance(onImportedListener: T?): NotebookImportDialog
			where T : NotebookImportedListener, T : Fragment
			= NotebookImportDialog::class.java.newInstance().apply {
			setTargetFragment(onImportedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val view = View.inflate(context, R.layout.dialog_notebook_import, null)
		
		val defaultDir = Environment.getExternalStorageDirectory().resolve("ZKLRussian")
		view.et_path.setText(defaultDir.path)
		
		return AlertDialog.Builder(context)
			.setTitle(R.string.import_Notebook)
			.setView(view)
			.setPositiveButton(R.string.ok) { _, _ ->
				val activity = mainActivity
				val listener = targetFragment as? NotebookImportedListener
				val bookShelf = myApp.notebookShelf
				activity.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,true) { granted, _ ->
					if (!granted) activity.toast(R.string.no_sdcard_read_permission)
					else launch(CommonPool) {
						try {
							val file = File(view.et_path.text.toString())
							val (key, _) = bookShelf.importNotebook(file)
							launch(UI) {
								listener?.onNotebookImported(key)
								activity.toast(R.string.import_succeed)
							}
						} catch (e: Exception) {
							e.printStackTrace()
							launch(UI) { activity.toast(R.string.import_failed) }
						}
					}
				}
			}
			.setNegativeButton(R.string.cancel, null)
			.create()
	}
	
}
