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
import com.zkl.zklRussian.core.note.NoteMemoryStatus


class NoteViewFragment : NoteHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long)
			= NoteViewFragment::class.java.newInstance(notebookKey, noteId)
	}
	
	
	//view
	private lateinit var tv_title: TextView
	private lateinit var tv_info: TextView
	private lateinit var b_edit: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_view, container, false).apply {
		tv_title = findViewById(R.id.tv_title) as TextView
		tv_info = findViewById(R.id.tv_info) as TextView
		b_edit = findViewById(R.id.b_edit) as Button
	}
	override fun onStart() {
		super.onStart()
		
		//刷新缓存的词条
		if (tryLoadNote() == null) {
			fragmentManager.popBackStack()
			return
		}
		
		tv_title.text = getString(R.string.Note_view_id, noteId)
		
		if (note.memoryState.status != NoteMemoryStatus.infant) {
			val relativeReviewTime = (note.memoryState.reviewTime - System.currentTimeMillis()).toDouble()/(3600*1000)
			tv_info.text = getString(R.string.Note_view_info,
				note.memoryState.progress.toInt(), note.memoryState.load.toInt(), relativeReviewTime)
				.replace("\\n", "\n")
		} else {
			tv_info.visibility = View.GONE
		}
		
		b_edit.isEnabled = notebook is MutableNotebook
		b_edit.setOnClickListener {
			val fragment = NoteEditFragment.newInstance(notebookKey, noteId)
			fragmentManager.jumpTo(fragment,true)
		}
		
		updateNoteContent()
		
	}
	
	
	//noteContent
	private var noteContentViewFragment: NoteContentViewFragment? = null
	override fun onAttachFragment(childFragment: Fragment) {
		super.onAttachFragment(childFragment)
		noteContentViewFragment = childFragment as? NoteContentViewFragment
	}
	private fun updateNoteContent() {
		val noteContent = note.content
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


