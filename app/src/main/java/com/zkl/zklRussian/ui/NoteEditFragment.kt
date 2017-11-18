package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.ConflictException
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.NoteMemoryState
import com.zkl.zklRussian.core.note.QuestionContent
import org.jetbrains.anko.support.v4.toast

class NoteEditFragment : NoteHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long)
			= NoteEditFragment::class.java.newInstance(notebookKey, noteId)
	}
	
	private val isCreateMode get() = noteId==-1L
	
	//view
	private lateinit var tv_title: TextView
	private lateinit var b_delete: Button
	private lateinit var cb_remainProgress:CheckBox
	private lateinit var b_ok: Button
	private lateinit var b_cancel: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_edit, container, false).apply {
		
		tv_title = findViewById(R.id.tv_title) as TextView
		b_delete = findViewById(R.id.b_delete) as Button
		
		cb_remainProgress = findViewById(R.id.cb_remainProgress) as CheckBox
		
		b_ok = findViewById(R.id.b_ok) as Button
		b_cancel = findViewById(R.id.b_cancel) as Button
		
	}
	override fun onStart() {
		super.onStart()
		
		if (isCreateMode) {
			tv_title.text = getString(R.string.Note_create)
			b_delete.visibility=View.GONE
			
			updateNoteContent(QuestionContent("", ""))
			
			cb_remainProgress.visibility = View.GONE
			
			b_ok.setOnClickListener {
				val newNoteContent = noteContentEditFragment!!.applyChange()
				try {
					mutableNotebook.addNote(newNoteContent)
					fragmentManager.popBackStack()
				} catch (e: ConflictException) {
					toast(getString(R.string.there_are_conflicted_notes))
				}
			}
			b_cancel.setOnClickListener {
				fragmentManager.popBackStack()
			}
			
		}
		else{
			
			if (tryLoadNote() == null) {
				fragmentManager.popBackStack()
				return
			}
			
			tv_title.text = getString(R.string.Note_edit_id, noteId)
			b_delete.setOnClickListener {
				NoteDeleteDialog.newInstance(notebookKey,noteId).show(fragmentManager,null)
			}
			
			updateNoteContent()
			
			cb_remainProgress.visibility = View.VISIBLE
			
			b_ok.setOnClickListener {
				val newNoteContent = noteContentEditFragment!!.applyChange()
				try {
					mutableNotebook.modifyNoteContent(noteId, newNoteContent)
					if (!cb_remainProgress.isChecked)
						mutableNotebook.modifyNoteMemory(noteId, NoteMemoryState.beginningState())
					fragmentManager.popBackStack()
				} catch (e: ConflictException) {
					toast(getString(R.string.there_are_conflicted_notes))
				}
			}
			b_cancel.setOnClickListener {
				fragmentManager.popBackStack()
			}
			
		}
		
	}
	
	//noteContent
	private var noteContentEditFragment: NoteContentEditFragment? = null
	override fun onAttachFragment(childFragment: Fragment) {
		super.onAttachFragment(childFragment)
		noteContentEditFragment = childFragment as? NoteContentEditFragment
	}
	private fun updateNoteContent(noteContent: NoteContent=note.content){
		if (noteContentEditFragment?.isCompatible(noteContent) == true) {
			noteContentEditFragment?.noteContent = noteContent
		} else {
			val fragment = typedNoteContentEditFragments[noteContent.typeTag]?.newInstance()
				?: throw RuntimeException("The noteContent type \"${noteContent.typeTag}\" is not supported.")
			childFragmentManager.beginTransaction()
				.replace(R.id.fl_noteContent_container, fragment)
				.commit()
			fragment.noteContent = noteContent
			noteContentEditFragment = fragment
		}
	}
	
}

