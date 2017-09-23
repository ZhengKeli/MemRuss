package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Note
import com.zkl.zklRussian.core.note.Notebook

abstract class NotebookHoldingFragment: Fragment {
	
	constructor():super()
	constructor(notebookKey: Int):super(){
		this.notebookKey=notebookKey
	}
	
	//key
	var notebookKey: Int = -1
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		notebookKey = savedInstanceState?.getInt(this::notebookKey.name) ?: notebookKey
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putInt(this::notebookKey.name, notebookKey)
	}
	
	//apis
	protected var _notebook: Notebook? = null
	protected val notebook: Notebook get() = _notebook ?: myApp.noteManager.getRegisterNotebook(notebookKey)!!
	protected val mutableNotebook: MutableNotebook get() = notebook as MutableNotebook
}

abstract class NoteHoldingFragment: NotebookHoldingFragment {
	
	constructor():super()
	constructor(notebookKey: Int, noteId: Long) : super(notebookKey) {
		this.noteId = noteId
	}
	
	//ids
	protected var noteId: Long = -1L
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		noteId = savedInstanceState?.getLong(this::noteId.name)?:noteId
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putLong(this::noteId.name, noteId)
	}
	
	//api
	protected var _note: Note? = null
	protected val note: Note get() = _note ?: notebook.getNote(noteId)
	
}

