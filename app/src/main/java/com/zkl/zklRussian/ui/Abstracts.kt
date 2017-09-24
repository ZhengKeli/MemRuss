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
	protected var notebookKey: Int = -1
		private set
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		notebookKey = savedInstanceState?.getInt(this::notebookKey.name) ?: notebookKey
		if(notebookKey!=-1) _notebook = myApp.noteManager.getRegisterNotebook(notebookKey)!!
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putInt(this::notebookKey.name, notebookKey)
	}
	
	//apis
	private var _notebook:Notebook? = null
	protected val notebook: Notebook get() = _notebook!!
	protected val mutableNotebook: MutableNotebook get() = notebook as MutableNotebook
}

abstract class NoteHoldingFragment: NotebookHoldingFragment {
	
	constructor():super()
	constructor(notebookKey: Int, noteId: Long) : super(notebookKey) {
		this.noteId = noteId
	}
	
	//ids
	protected var noteId: Long = -1L
		private set
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		noteId = savedInstanceState?.getLong(this::noteId.name)?:noteId
		if(noteId!=-1L) _note =  notebook.getNote(noteId)
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putLong(this::noteId.name, noteId)
	}
	
	//api
	private var _note: Note? = null
	val note:Note get() = _note!!
	
}

