package com.zkl.ZKLRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.zkl.ZKLRussian.R
import com.zkl.ZKLRussian.control.myApp
import com.zkl.ZKLRussian.core.note.MutableNotebook
import com.zkl.ZKLRussian.core.note.Note
import com.zkl.ZKLRussian.core.note.Notebook

class NoteViewerFragment() : Fragment() {
	
	//view
	private lateinit var b_edit: Button
	private lateinit var b_delete: Button
	private lateinit var fl_noteView: FrameLayout
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_viewer, container, false).also { rootView ->
		
		b_edit = rootView.findViewById(R.id.b_edit) as Button
		b_delete = rootView.findViewById(R.id.b_delete) as Button
		fl_noteView = rootView.findViewById(R.id.fl_noteView) as FrameLayout
	}
	override fun onStart() {
		super.onStart()
		
		var deleteConfirmed = false
		b_delete.setOnClickListener {
			if (!deleteConfirmed) {
				deleteConfirmed = true
				b_delete.setText("确认删除")
			} else {
				deleteConfirmed = false
				b_delete.setText("删除")
				
				processDeleteNote()
				notebookActivity.jumpBackFragment()
			}
		}
		
		//todo showNote
		
	}
	
	
	//key
	private var notebookKey:Int = -1
	private var noteId:Long = -1
	constructor(notebookKey:Int,noteId:Long):this(){
		this.notebookKey = notebookKey
		this.noteId = noteId
	}
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		notebookKey = savedInstanceState?.getInt(this::notebookKey.name) ?: notebookKey
		noteId = savedInstanceState?.getLong(this::noteId.name)?:noteId
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putInt(this::notebookKey.name, notebookKey)
		outState.putLong(this::noteId.name, noteId)
	}
	
	
	//note
	private val _notebook: Notebook by lazy { myApp.noteManager.getRegisterNotebook(notebookKey)!! }
	private val notebook: Notebook get() = _notebook
	private val mutableNotebook: MutableNotebook get() = notebook as MutableNotebook
	private fun processDeleteNote(){
		mutableNotebook.deleteNote(noteId)
	}
	
	private var _note:Note? = null
	private val note:Note get() = _note?: _notebook.getNote(noteId)
	
}



