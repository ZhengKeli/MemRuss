package com.zkl.memruss.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.View
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookKey
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
		
		view.et_path.run {
			val defaultDir = Environment.getExternalStorageDirectory().resolve("ZKLRussian")
			val pathText = defaultDir.path+"/"
			setText(pathText)
			setSelection(text.length)
			addOnAttachStateChangeListener(object :View.OnAttachStateChangeListener{
				override fun onViewDetachedFromWindow(v: View?) {}
				override fun onViewAttachedToWindow(v: View?) = showSoftInput()
			})
		}
		
		return AlertDialog.Builder(context)
			.setTitle(R.string.import_Notebook)
			.setView(view)
			.setPositiveButton(R.string.ok) { _, _ ->
				val activity = mainActivity
				val listener = targetFragment as? NotebookImportedListener
				val bookShelf = myApp.notebookShelf
				activity.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,true) { granted, _ ->
					if (!granted) {
						activity.toast(R.string.no_sdcard_read_permission)
						return@requestPermission
					}
					launch(CommonPool) {
						val (key, _) = try {
							val file = File(view.et_path.text.toString())
							bookShelf.importNotebook(file)
						} catch (e: Exception) {
							e.printStackTrace()
							null
						} ?: kotlin.run {
							launch(UI) { activity.toast(R.string.import_failed) }
							return@launch
						}
						launch(UI) { listener?.onNotebookImported(key) }
					}
				}
			}
			.setNegativeButton(R.string.cancel, null)
			.create()
	}
	
}
