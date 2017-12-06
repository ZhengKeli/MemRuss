package com.zkl.zklRussian.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.DialogFragment
import android.view.View
import android.widget.EditText
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookBrief
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.find
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
		val brief = arguments[arg_notebookBrief] as NotebookBrief
		
		val view = View.inflate(context, R.layout.dialog_notebook_export, null)
		val et_path: EditText = view.find(R.id.et_path)
		
		val format = SimpleDateFormat("YYYY-MM-dd", Locale.getDefault())
		val targetFileName = brief.bookName+"-"+format.format(Date())+".zrb"
		val targetFile = Environment.getExternalStorageDirectory().resolve("ZKLRussian").resolve(targetFileName)
		et_path.setText(targetFile.path)
		
		return AlertDialog.Builder(context)
			.setTitle(R.string.export_Notebook)
			.setView(view)
			.setPositiveButton(R.string.ok) { _, _ ->
				val activity = mainActivity
				val bookShelf = myApp.notebookShelf
				activity.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,true) { granted, _ ->
					if (!granted) activity.toast(R.string.no_sdcard_permission)
					else launch(CommonPool) {
						try {
							val target = File(et_path.text.toString())
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
