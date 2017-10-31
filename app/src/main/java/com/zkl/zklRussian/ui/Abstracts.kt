package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDialogFragment
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Note
import com.zkl.zklRussian.core.note.Notebook

internal fun Bundle.getNotebookKey(key: String): NotebookKey? = getSerializable(key) as? NotebookKey
internal fun Bundle.putNotebookKey(key: String, value: NotebookKey) = putSerializable(key, value)

abstract class NotebookHoldingFragment: Fragment() {
	
	//notebookKey
	var notebookKey: NotebookKey? = null
		set(value) {
			field = value
			_notebook = null
		}
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		if (notebookKey == null) notebookKey = savedInstanceState?.getNotebookKey(this::notebookKey.name)
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		notebookKey?.let { outState.putNotebookKey(this::notebookKey.name, it) }
	}
	
	//notebook
	private var _notebook: Notebook? = null
		get() {
			if (field == null) {
				field = notebookKey?.let { myApp.notebookShelf.restoreOpenedNotebook(it) }
			}
			return field
		}
	protected val notebook: Notebook get() = _notebook!!
	protected val mutableNotebook: MutableNotebook get() = notebook as MutableNotebook
	
}
abstract class NoteHoldingFragment : NotebookHoldingFragment() {
	
	//noteId
	var noteId: Long = -1L
		set(value) {
			field = value
			_note = null
		}
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		noteId = noteId.takeIf { it != -1L } ?: savedInstanceState?.getLong(this::noteId.name) ?: -1L
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putLong(this::noteId.name, noteId)
	}
	
	//note
	private var _note: Note? = null
		get() {
			if (field == null) {
				field = noteId.takeIf { it!=-1L }?.let { notebook.getNote(it) }
			}
			return field
		}
	val note:Note get() = _note!!
	
}

abstract class NotebookHoldingDialog: AppCompatDialogFragment(){
	
	//notebookKey
	var notebookKey: NotebookKey? = null
		set(value) {
			field = value
			_notebook = null
		}
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		if (notebookKey == null) notebookKey = savedInstanceState?.getNotebookKey(this::notebookKey.name)
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		notebookKey?.let { outState.putNotebookKey(this::notebookKey.name, it) }
	}
	
	//notebook
	private var _notebook: Notebook? = null
		get() {
			if (field == null) {
				field = notebookKey?.let { myApp.notebookShelf.restoreOpenedNotebook(it) }
			}
			return field
		}
	protected val notebook: Notebook get() = _notebook!!
	protected val mutableNotebook: MutableNotebook get() = notebook as MutableNotebook
	
}
abstract class NoteHoldingDialog: NotebookHoldingDialog(){
	
	//noteId
	var noteId: Long = -1L
		set(value) {
			field = value
			_note = null
		}
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		noteId = noteId.takeIf { it != -1L } ?: savedInstanceState?.getLong(this::noteId.name) ?: -1L
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putLong(this::noteId.name, noteId)
	}
	
	//note
	private var _note: Note? = null
		get() {
			if (field == null) {
				field = noteId.takeIf { it!=-1L }?.let { notebook.getNote(it) }
			}
			return field
		}
	val note:Note get() = _note!!
	
}
