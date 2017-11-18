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

class NotebookFragment : NotebookHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= NotebookFragment::class.java.newInstance(notebookKey)
	}
	
	private lateinit var b_back: ImageButton
	private lateinit var tv_title: TextView
	private lateinit var sv_search:SearchView
	private lateinit var cl_infoBar:ConstraintLayout
	private lateinit var tv_bookInfo:TextView
	private lateinit var b_memoryPlan:ImageButton
	private lateinit var b_addNote:ImageButton
	private lateinit var cl_review:ConstraintLayout
	private lateinit var tv_review:TextView
	private lateinit var b_review:Button
	private lateinit var lv_notes:ListView
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook, container, false).apply {
		
		b_back = findViewById(R.id.b_back) as ImageButton
		tv_title = findViewById(R.id.tv_title) as TextView
		sv_search = findViewById(R.id.sv_search) as SearchView
		cl_infoBar = findViewById(R.id.cl_infoBar) as ConstraintLayout
		tv_bookInfo = findViewById(R.id.tv_bookInfo) as TextView
		b_memoryPlan = findViewById(R.id.b_memoryPlan) as ImageButton
		b_addNote = findViewById(R.id.b_addNote) as ImageButton
		cl_review = findViewById(R.id.cl_review) as ConstraintLayout
		tv_review = findViewById(R.id.tv_review) as TextView
		b_review = findViewById(R.id.b_review) as Button
		lv_notes = findViewById(R.id.lv_notes) as ListView
		
	}
	override fun onStart() {
		super.onStart()
		
		b_back.setOnClickListener {
			fragmentManager.popBackStack()
		}
		
		initializeNotebookViews()
		initializeNoteList()
		
	}
	
	//notebook views
	private fun initializeNotebookViews() {
		//title
		tv_title.text = notebook.name
		
		//search
		sv_search.setOnSearchClickListener {
			fragmentManager.jumpTo(NotebookSearchFragment.newInstance(notebookKey),true)
			sv_search.isIconified = true
		}
		
		//info
		tv_bookInfo.text = getString(R.string.count_Notes_in_all, notebook.noteCount)
		b_addNote.setOnClickListener {
			val fragment = NoteEditFragment.newInstance(notebookKey, -1)
			fragmentManager.jumpTo(fragment,true)
		}
		b_memoryPlan.setOnClickListener{
			fragmentManager.jumpTo(MemoryPlanFragment.newInstance(notebookKey),true)
		}
		if (notebook is MutableNotebook) {
			b_addNote.visibility = View.VISIBLE
			b_memoryPlan.visibility = View.VISIBLE
		} else {
			b_addNote.visibility = View.GONE
			b_memoryPlan.visibility = View.GONE
		}
		
		//review
		if(notebook is MutableNotebook){
			b_review.setOnClickListener {
				fragmentManager.jumpTo(NoteReviewFragment.newInstance(notebookKey),true)
			}
			updateNeedReview()
		}
		
	}
	private fun updateNeedReview(){
		if (notebook is MutableNotebook) {
			mutableNotebook.fillNotesByPlan()
			val needReviewCount = notebook.countNeedReviewNotes(System.currentTimeMillis())
			if (needReviewCount > 0) {
				cl_review.visibility = View.VISIBLE
				tv_review.text = getString(R.string.need_review,needReviewCount)
			}
		}
	}
	
	private fun initializeNoteList() {
		notesBuffer.clearBuffer()
		lv_notes.adapter = object :NoteListAdapter(){
			override fun getCount() = notesBuffer.size
			override fun getItem(position: Int) = notesBuffer[position]
			override val context: Context get() = activity
		}
		lv_notes.setOnItemClickListener { _, view, _, _ ->
			view as NoteItemView
			val fragment = NoteViewFragment.newInstance(notebookKey, view.note!!.id)
			fragmentManager.jumpTo(fragment,true)
		}
	}
	private fun updateNoteList(){
		(lv_notes.adapter as BaseAdapter).notifyDataSetChanged()
	}
	
	
	//notes buffer
	private val notesBuffer = object : SectionBufferList<Note>(){
		override fun getSection(startFrom: Int): List<Note>
			= notebook.selectLatestNotes(sectionSize, startFrom)
		override val size: Int
			get() = notebook.noteCount
	}
	
}

