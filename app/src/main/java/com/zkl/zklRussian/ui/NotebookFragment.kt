package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Note
import kotlinx.android.synthetic.main.fragment_notebook.*

class NotebookFragment : NotebookHoldingFragment(),
	NoteMenuDialog.NoteListChangedListener {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= NotebookFragment::class.java.newInstance(notebookKey)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		//top bar
		b_back.setOnClickListener {
			fragmentManager.popBackStack()
		}
		tv_title.text = notebook.name
		sv_search.setOnSearchClickListener {
			NotebookSearchFragment.newInstance(notebookKey).jump(fragmentManager, true)
			sv_search.isIconified = true
		}
		
		//info bar
		tv_bookInfo.text = getString(R.string.count_Notes_in_all, notebook.noteCount)
		if (notebook is MutableNotebook) {
			b_addNote.visibility = View.VISIBLE
			b_addNote.setOnClickListener {
				NoteEditFragment.newInstance(notebookKey, -1).jump(fragmentManager, true)
			}
			b_memoryPlan.visibility = View.VISIBLE
			b_memoryPlan.setOnClickListener {
				MemoryPlanFragment.newInstance(notebookKey).jump(fragmentManager, true)
			}
		} else {
			b_addNote.visibility = View.GONE
			b_memoryPlan.visibility = View.GONE
		}
		
		//review bar
		if (notebook is MutableNotebook) {
			b_review.setOnClickListener {
				NoteReviewFragment.newInstance(notebookKey).jump(fragmentManager, true)
			}
			updateNeedReview()
		}
		
		//list
		notesBuffer.clearBuffer()
		lv_notes.adapter = object : NoteListAdapter() {
			override fun getCount() = notesBuffer.size
			override fun getItem(position: Int) = notesBuffer[position]
			override val context: Context get() = activity
		}
		lv_notes.setOnItemClickListener { _, _, position, _ ->
			val note = notesBuffer[position]
			NoteViewFragment.newInstance(notebookKey, note.id).jump(fragmentManager, true)
		}
		lv_notes.setOnItemLongClickListener { _, _, position, _ ->
			val note = notesBuffer[position]
			NoteMenuDialog.newInstance(notebookKey, note.id, this@NotebookFragment).show(fragmentManager)
			true
		}
	}
	
	override fun onNoteListChanged() {
		updateList()
	}
	
	private fun updateList(){
		notesBuffer.clearBuffer()
		(lv_notes.adapter as? BaseAdapter)?.notifyDataSetChanged()
		updateNeedReview()
	}
	
	private fun updateNeedReview() {
		if (notebook is MutableNotebook) {
			mutableNotebook.activateNotesByPlan()
			val needReviewCount = notebook.countNeedReviewNotes(System.currentTimeMillis())
			if (needReviewCount > 0) {
				cl_review.visibility = View.VISIBLE
				tv_review.text = getString(R.string.need_review, needReviewCount)
			}
		}
	}
	
	private val notesBuffer = object : SectionBufferList<Note>() {
		override fun getSection(startFrom: Int): List<Note>
			= notebook.selectLatestNotes(sectionSize, startFrom)
		
		override val size: Int get() = notebook.noteCount
	}
	
	
}

