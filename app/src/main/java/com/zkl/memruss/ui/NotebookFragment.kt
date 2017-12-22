package com.zkl.memruss.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.zkl.memruss.R
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.Note
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
		
		val key = notebookKey
		
		//top bar
		b_back.setOnClickListener {
			fragmentManager.popBackStack()
		}
		tv_title.text = notebook.name
		sv_search.setOnSearchClickListener {
			NotebookSearchFragment.newInstance(key).jump(fragmentManager)
			sv_search.isIconified = true
		}
		
		//info bar
		tv_bookInfo.text = getString(R.string.count_Notes_in_all, notebook.noteCount)
		if (key.mutable) {
			b_addNote.visibility = View.VISIBLE
			b_addNote.setOnClickListener {
				NoteEditFragment.newInstance(key, -1).jump(fragmentManager)
			}
			b_memoryPlan.visibility = View.VISIBLE
			b_memoryPlan.setOnClickListener {
				MemoryPlanFragment.newInstance(key).jump(fragmentManager)
			}
		} else {
			b_addNote.visibility = View.GONE
			b_memoryPlan.visibility = View.GONE
		}
		
		//review bar
		b_review.setOnClickListener {
			NoteReviewFragment.newInstance(key).jump(fragmentManager)
		}
		updateNeedReview()
		
		//list
		lv_notes.adapter = object : NoteListAdapter() {
			override fun getCount() = notesBuffer.getSizeAndExpand()
			override fun getItem(position: Int) = notesBuffer.getAndExpand(position)
			override val context: Context get() = activity
		}
		lv_notes.setOnItemClickListener { _, _, position, _ ->
			val note = notesBuffer[position]
			NoteViewFragment.newInstance(key, note.id).jump(fragmentManager)
		}
		lv_notes.setOnItemLongClickListener { _, _, position, _ ->
			val note = notesBuffer[position]
			NoteMenuDialog.newInstance(key, note.id, this@NotebookFragment).show(fragmentManager)
			true
		}
		updateList()
		
	}
	
	override fun onNoteListChanged() {
		updateList()
		updateNeedReview()
	}
	
	private val notesBuffer = AutoExpandBuffer<Note>()
	private fun updateList() {
		notesBuffer.setSource(notebook.noteCount) { offset, limit ->
			lv_notes.post {
				(lv_notes.adapter as? BaseAdapter)?.notifyDataSetChanged()
			}
			notebook.selectLatestNotes(limit, offset)
		}
		(lv_notes.adapter as? BaseAdapter)?.notifyDataSetChanged()
	}
	
	private fun updateNeedReview() {
		val key = notebookKey
		if (key.mutable) mutableNotebook.activateNotesByPlan()
		
		val needReviewCount = notebook.countNeedReviewNotes(System.currentTimeMillis())
		if (needReviewCount > 0) {
			cl_review.visibility = View.VISIBLE
			tv_review.text = getString(R.string.need_review, needReviewCount)
			b_review.isEnabled = key.mutable
		}
	}
	
}

