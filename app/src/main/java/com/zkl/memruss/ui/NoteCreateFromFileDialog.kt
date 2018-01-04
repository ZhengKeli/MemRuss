package com.zkl.memruss.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.View
import android.widget.SimpleAdapter
import com.zkl.memruss.R
import com.zkl.memruss.control.note.NoteContentTextParser
import com.zkl.memruss.control.note.NotebookCompactor
import kotlinx.android.synthetic.main.dialog_note_create_from_file.view.*
import java.io.File
import java.nio.charset.Charset

class NoteCreateFromFileDialog : DialogFragment() {
	
	interface NotesFileSelectedListener {
		fun onNotesFileSelected(file: File, charset: Charset)
	}
	
	companion object {
		fun <T> newInstance(deletedListener: T?): NoteCreateFromFileDialog
			where T : NotesFileSelectedListener, T : Fragment
			= NoteCreateFromFileDialog::class.java.newInstance().apply {
			setTargetFragment(deletedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val defaultPath = NotebookCompactor.defaultImportDir.resolve("notes.${NoteContentTextParser.fileExtension}").path
		val charsets = arrayOf("ANSI/GBK" to "GBK", "UTF-8" to "UTF-8", "UTF-16/Unicode" to "UTF-16", "ASCII" to "ASCII")
			.mapNotNull { if (Charset.isSupported(it.second)) it.first to Charset.forName(it.second) else null }
		
		val view = View.inflate(context, R.layout.dialog_note_create_from_file, null)
		view.et_path.setText(defaultPath)
		view.sp_charset.adapter = SimpleAdapter(context,
			charsets.map { mapOf("name" to it.first) },R.layout.adapter_charset,
			arrayOf("name"), intArrayOf(R.id.tv_charset))
		
		return AlertDialog.Builder(context)
			.setTitle(R.string.create_notes_from_file)
			.setView(view)
			.setPositiveButton(R.string.ok) { _, _ ->
				val file = File(view.et_path.text.toString())
				val charset = charsets[view.sp_charset.selectedItemPosition].second
				(targetFragment as? NotesFileSelectedListener)?.onNotesFileSelected(file, charset)
			}
			.setNegativeButton(R.string.cancel,null)
			.create()
	}
}
