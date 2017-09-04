package com.zkl.ZKLRussian.ui

import android.os.Bundle
import com.zkl.ZKLRussian.core.note.Note

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