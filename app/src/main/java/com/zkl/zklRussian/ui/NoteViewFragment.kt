package com.zkl.zklRussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
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
	private lateinit var b_edit: Button
	private lateinit var tv_info: TextView
	private lateinit var fl_noteContent: FrameLayout
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_view, container, false).apply {
		tv_title = findViewById(R.id.tv_title)
		b_edit = findViewById(R.id.b_edit)
		tv_info = findViewById(R.id.tv_info)
		fl_noteContent = findViewById(R.id.fl_noteContent)
	}.apply {
		
		//刷新缓存的词条
		if (tryLoadNote() == null) {
			fragmentManager.popBackStack()
			return@apply
		}
		
		tv_title.text = getString(R.string.Note_view_id, noteId)
		
		if (note.memoryState.status != NoteMemoryStatus.infant) {
			val relativeReviewTime = (note.memoryState.reviewTime - System.currentTimeMillis()).toDouble() / (3600 * 1000)
			tv_info.text = getString(R.string.Note_view_info,
				note.memoryState.progress.toInt(), note.memoryState.load.toInt(), relativeReviewTime)
				.replace("\\n", "\n")
		} else {
			tv_info.visibility = View.GONE
		}
		
		b_edit.isEnabled = notebook is MutableNotebook
		b_edit.setOnClickListener {
			NoteEditFragment.newInstance(notebookKey, noteId).jump(fragmentManager, true)
		}
		
		noteContentViewHolder = null
		updateNoteContent()
		
	}
	
	
	//noteContent
	private var noteContentViewHolder: NoteContentViewHolder? = null
	
	private fun updateNoteContent() {
		val noteContent = note.content
		val oldHolder = noteContentViewHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
		} else {
			val holder = typedNoteContentViewHolders[noteContent.typeTag]?.invoke(activity, fl_noteContent)
				?: throw RuntimeException("The noteContent type \"${noteContent.typeTag}\" is not supported.")
			holder.noteContent = noteContent
			fl_noteContent.removeAllViews()
			fl_noteContent.addView(holder.view)
			noteContentViewHolder = holder
		}
	}
	
	
}


