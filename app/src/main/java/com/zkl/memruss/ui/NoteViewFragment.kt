package com.zkl.memruss.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.base.getNoteOrNull
import com.zkl.memruss.core.note.base.isLearning
import kotlinx.android.synthetic.main.fragment_note_view.*
import kotlin.math.abs


class NoteViewFragment : Fragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey, noteId: Long): NoteViewFragment {
			return NoteViewFragment::class.java.newInstance(notebookKey, noteId)
		}
	}
	
	//view
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.fragment_note_view, container, false)
	}
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		//read note
		val notebookKey = argNotebookKey
		val noteId = argNoteId
		val notebook = myApp.notebookShelf.restoreNotebook(notebookKey)
		val note = notebook.getNoteOrNull(noteId) ?: kotlin.run { fragmentManager.popBackStack(); return }
		
		
		//setup views
		tv_title.text = getString(R.string.Note_view_id, noteId)
		
		if (note.isLearning) {
			val memoryState = note.memoryState
			val progressText = memoryState.progress.let { progress ->
				val percent = (100 * progress / 20).toInt()
				"$percent%"
			}
			val loadText = memoryState.load.let { load ->
				"%.2f".format(load)
			}
			val reviewTimeText = (note.memoryState.reviewTime - System.currentTimeMillis()).let { ms ->
				val hour = ms / (3600 * 1000)
				val min = abs(ms / (1000 * 60) % 60)
				if (min == 0L) "$hour"
				else "$hour:$min"
			}
			tv_info.text = getString(R.string.Note_view_info, progressText, loadText, reviewTimeText)
				.replace("\\n", "\n")
		} else {
			tv_info.visibility = View.GONE
		}
		
		b_edit.isEnabled = notebookKey.mutable
		b_edit.setOnClickListener {
			NoteEditFragment.newInstance(notebookKey, noteId).jumpFade(fragmentManager)
		}
		
		noteContentViewHolder = null
		updateNoteContent(note.content)
	}
	
	//noteContent
	private var noteContentViewHolder: NoteContentViewHolder? = null
	
	private fun updateNoteContent(noteContent: NoteContent) {
		val oldHolder = noteContentViewHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
		} else {
			val holder = noteContent.newViewHolderOrThrow(context, fl_noteContent)
			fl_noteContent.removeAllViews()
			fl_noteContent.addView(holder.view)
			noteContentViewHolder = holder
		}
	}
	
}
