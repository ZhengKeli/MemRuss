package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Note
import org.jetbrains.anko.find

class NotebookFragment : NotebookHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= NotebookFragment::class.java.newInstance(notebookKey)
	}
	
	lateinit var b_back: ImageButton
	lateinit var tv_title: TextView
	lateinit var sv_search: SearchView
	lateinit var tv_bookInfo: TextView
	lateinit var b_memoryPlan: ImageButton
	lateinit var b_addNote: ImageButton
	lateinit var cl_review: ConstraintLayout
	lateinit var tv_review: TextView
	lateinit var b_review: Button
	lateinit var lv_notes: ListView
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook, container, false).apply {
		b_back = find(R.id.b_back)
		tv_title = find(R.id.tv_title)
		sv_search = find(R.id.sv_search)
		tv_bookInfo = find(R.id.tv_bookInfo)
		b_memoryPlan = find(R.id.b_memoryPlan)
		b_addNote = find(R.id.b_addNote)
		cl_review = find(R.id.cl_review)
		tv_review = find(R.id.tv_review)
		b_review = find(R.id.b_review)
		lv_notes = find(R.id.lv_notes)
	}.apply {
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
			NoteMenuDialog.newInstance(notebookKey, note.id).show(fragmentManager, null)
			true
		}
	}
	
	private fun updateNeedReview() {
		if (notebook is MutableNotebook) {
			mutableNotebook.fillNotesByPlan()
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

