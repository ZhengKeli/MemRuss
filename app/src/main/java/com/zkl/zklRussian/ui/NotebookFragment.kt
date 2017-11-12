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
import com.zkl.zklRussian.core.note.QuestionContent
import java.util.*

class NotebookFragment : NotebookHoldingFragment(),BackPressedHandler {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= NotebookFragment::class.java.newInstance(notebookKey)
	}
	
	//views
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
		initializeSearchView()
		initializeNoteList()
	}
	
	//notebook views
	private fun initializeNotebookViews() {
		//title
		tv_title.text = notebook.name
		
		//info
		tv_bookInfo.text = getString(R.string.count_NotesInAll, notebook.noteCount)
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
		val needReviewCount = notebook.countNeedReviewNotes(System.currentTimeMillis())
		if (needReviewCount>0) {
			cl_review.visibility = View.VISIBLE
			tv_review.text = getString(R.string.needReview,needReviewCount)
		}
	}
	
	//note list
	private lateinit var showingNotes:List<Note>
	private fun initializeNoteList() {
		notesBuffer.clearBuffer()
		showingNotes = notesBuffer
		lv_notes.adapter = object : BaseAdapter() {
			override fun getItem(position: Int) = showingNotes[getItemId(position).toInt()]
			override fun getItemId(position: Int) = position.toLong()
			
			override fun getCount() = showingNotes.size
			override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
				val view = (convertView as? NoteItemView) ?: NoteItemView(activity)
				view.note = getItem(position)
				return view
			}
		}
		lv_notes.setOnItemClickListener { _, view, _, _ ->
			view as NoteItemView
			val fragment = NoteViewFragment.newInstance(notebookKey, view.note!!.id)
			fragmentManager.jumpTo(fragment,true)
		}
	}
	
	
	//search
	var searchMode: Boolean = false
		set(value) {
			if (field == value) return
			field = value
			if (value) {
				tv_title.visibility = View.GONE
				b_back.visibility = View.GONE
				cl_infoBar.visibility = View.GONE
				sv_search.layoutParams.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD
				
				showingNotes = searchResult
				//todo start search thread
			} else {
				tv_title.visibility = View.VISIBLE
				b_back.visibility = View.VISIBLE
				cl_infoBar.visibility = View.VISIBLE
				sv_search.layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
				sv_search.isIconified = true
				
				showingNotes = notesBuffer
				//todo stop search thread
			}
		}
	override fun onBackPressed(): Boolean {
		return if (searchMode) {
			searchMode=false
			true
		}else false
	}
	private var searchResult:List<Note> = emptyList()
	private fun initializeSearchView(){
		sv_search.setOnSearchClickListener {
			searchMode =true
		}
		sv_search.setOnCloseListener {
			searchMode =false
			false
		}
		sv_search.setOnQueryTextListener(
			object:SearchView.OnQueryTextListener{
				override fun onQueryTextSubmit(query: String): Boolean {
					searchText(query)
					return true
				}
				override fun onQueryTextChange(newText: String): Boolean {
					searchText(newText)
					return true
				}
				fun searchText(text:String){
					TODO("search by $text")
				}
			}
		)
		searchMode = false
	}
	
	
	
	//notes buffer
	private val notesBuffer = object : SectionBufferList<Note>(){
		override fun getSection(startFrom: Int): List<Note>
			= notebook.selectLatestNotes(sectionSize, startFrom)
		override val size: Int
			get() = notebook.noteCount
	}
	
	
	
	
}

class NoteItemView(context: Context) : LinearLayout(context) {
	private val tv_title: TextView
	private val tv_content: TextView
	
	init {
		LayoutInflater.from(context).inflate(R.layout.adapter_note_item, this, true)
		tv_title = this.findViewById(R.id.tv_title) as TextView
		tv_content = this.findViewById(R.id.tv_content) as TextView
	}
	
	var note: Note? = null
		set(value) {
			field = value
			if (value == null) return
			
			val content = value.content
			when (content) {
				is QuestionContent -> {
					this.post {
						tv_title.text = content.question
						tv_content.text = content.answer
					}
				}
				else -> {
				}
			}
			
		}
}

abstract class SectionBufferList<T>
constructor(val sectionSize: Int = 20, val sectionCount: Int = 10)
	: AbstractList<T>() {
	
	private val sections = LinkedList<List<T>>()
	private var bufferFrom:Int = 0
	private val bufferSize:Int get() {
			return if (sections.isEmpty()) 0
			else sectionSize * (sections.size - 1) + sections.last.size
	}
	private val bufferToExclusive: Int get() = bufferFrom + bufferSize
	@Synchronized override fun get(index: Int): T {
		while (index < bufferFrom) extendAtHead()
		while (index >= bufferToExclusive) appendAtTail()
		return sections[(index - bufferFrom) / sectionSize][(index - bufferFrom) % sectionSize]
	}
	@Synchronized private fun extendAtHead() {
		bufferFrom -= sectionSize
		sections.addFirst(getSection(bufferFrom))
		if (sections.size > sectionCount) sections.removeLast()
	}
	@Synchronized private fun appendAtTail(){
		sections.addLast(getSection(bufferToExclusive))
		if (sections.size > sectionCount) {
			sections.removeFirst()
			bufferFrom += sectionSize
		}
	}
	@Synchronized fun clearBuffer(){
		sections.clear()
	}
	
	abstract fun getSection(startFrom:Int):List<T>
	
}