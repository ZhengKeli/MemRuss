package com.zkl.memruss.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.MutableNotebook3Compactor
import com.zkl.memruss.control.note.NotebookBrief
import com.zkl.memruss.control.note.NotebookCompactor
import kotlinx.android.synthetic.main.dialog_notebook_export.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NotebookExportDialog : DialogFragment() {
	
	companion object {
		private val arg_notebookBrief = "notebookBrief"
		fun newInstance(notebookBrief: NotebookBrief): NotebookExportDialog
			= NotebookExportDialog::class.java.newInstance().apply {
			arguments += bundleOf(arg_notebookBrief to notebookBrief)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val view = View.inflate(context, R.layout.dialog_notebook_export, null)
		
		val brief = arguments[arg_notebookBrief] as NotebookBrief
		val format = SimpleDateFormat("YYYY-MM-dd", Locale.getDefault())
		val targetFileName = brief.bookName + "-" + format.format(Date()) + "." + MutableNotebook3Compactor.fileExtension
		val targetFile = NotebookCompactor.defaultExportDir.resolve(targetFileName)
		view.et_path.setText(targetFile.path)
		
		return AlertDialog.Builder(context)
			.setTitle(R.string.export_Notebook)
			.setView(view)
			.setPositiveButton(R.string.ok) { _, _ ->
				val activity = mainActivity
				val bookShelf = myApp.notebookShelf
				activity.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,true) { granted, _ ->
					if (!granted) activity.toast(R.string.no_sdcard_write_permission)
					else launch(CommonPool) {
						try {
							val target = File(view.et_path.text.toString())
							bookShelf.exportNotebook(brief.file, target)
							launch(UI) { activity.toast(R.string.export_succeed) }
						} catch (e: Exception) {
							e.printStackTrace()
							launch(UI) { activity.toast(R.string.export_failed) }
						}
					}
				}
			}
			.setNegativeButton(R.string.cancel, null)
			.create()
	}
	
}
