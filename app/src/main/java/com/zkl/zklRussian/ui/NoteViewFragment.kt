package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.NoteMemoryState


class NoteViewFragment : NoteHoldingFragment {
	
	constructor():super()
	constructor(notebookKey: NotebookKey, noteId: Long) : super(notebookKey,noteId)
	
	//view
	private lateinit var tv_title: TextView
	private lateinit var tv_info: TextView
	private lateinit var b_edit: Button
	private lateinit var b_delete: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_view, container, false).apply {
		tv_title = findViewById(R.id.tv_title) as TextView
		tv_info = findViewById(R.id.tv_info) as TextView
		b_edit = findViewById(R.id.b_edit) as Button
		b_delete = findViewById(R.id.b_delete) as Button
	}
	override fun onStart() {
		super.onStart()
		
		tv_title.text = getString(R.string.Note_view_id, noteId)
		if (note.memory.state != NoteMemoryState.infant) {
			tv_info.text = """
				memoryProgress:${note.memory.progress}
				sumLoad:${note.memory.load}
				reviewTime:${note.memory.reviewTime}
			""".trimMargin()
		} else {
			tv_info.visibility = View.GONE
		}
		
		b_edit.isEnabled = notebook is MutableNotebook
		b_edit.setOnClickListener {
			mainActivity.jumpToFragment(NoteEditFragment(notebookKey,noteId),true)
		}
		
		b_delete.isEnabled = notebook is MutableNotebook
		b_delete.setOnClickListener {
			NoteDeleteDialog(notebookKey, noteId).show(fragmentManager, null)
		}
		
		updateNoteContent(note.content)
		
	}
	
	
	//noteContent
	private var noteContentViewFragment: NoteContentViewFragment? = null
	override fun onAttachFragment(childFragment: Fragment) {
		super.onAttachFragment(childFragment)
		noteContentViewFragment = childFragment as? NoteContentViewFragment
	}
	private fun updateNoteContent(noteContent: NoteContent){
		if (noteContentViewFragment?.isCompatible(noteContent) == true) {
			noteContentViewFragment?.noteContent = noteContent
		} else {
			val fragment = typedNoteContentViewFragments[noteContent.typeTag]?.newInstance()
				?: throw RuntimeException("The noteContent type \"${noteContent.typeTag}\" is not supported.")
			childFragmentManager.beginTransaction()
				.replace(R.id.fl_noteContent_container, fragment)
				.commit()
			fragment.noteContent = noteContent
			noteContentViewFragment = fragment
		}
	}
	
	
}


