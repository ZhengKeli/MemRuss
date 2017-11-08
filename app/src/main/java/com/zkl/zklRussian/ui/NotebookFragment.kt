package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Note
import com.zkl.zklRussian.core.note.NotebookMemoryState
import com.zkl.zklRussian.core.note.QuestionContent
import java.util.*

class NotebookFragment : NotebookHoldingFragment(),BackPressedHandler {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= NotebookFragment::class.java.newInstance(notebookKey)
	}
	
	//views
	private lateinit var b_back: ImageButton
	private lateinit var tv_bookName: TextView
	private lateinit var tv_bookInfo:TextView
	private lateinit var b_addNote:ImageButton
	private lateinit var b_review:ImageButton
	private lateinit var sv_search:SearchView
	private lateinit var lv_notes:ListView
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook, container, false).apply {
		
		b_back = findViewById(R.id.b_back) as ImageButton
		tv_bookName = findViewById(R.id.tv_bookName) as TextView
		tv_bookInfo = findViewById(R.id.tv_bookInfo) as TextView
		b_addNote = findViewById(R.id.b_addNote) as ImageButton
		b_review = findViewById(R.id.b_review) as ImageButton
		sv_search = findViewById(R.id.sv_search) as SearchView
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
		tv_bookName.text = notebook.name
		tv_bookInfo.text = getString(R.string.count_NotesInAll, notebook.noteCount)
		
		b_addNote.setOnClickListener {
			val fragment = NoteEditFragment.newInstance(notebookKey, -1)
			fragmentManager.jumpTo(fragment,true)
		}
		b_review.setOnClickListener{
			when(notebook.memory.state) {
				NotebookMemoryState.learning -> fragmentManager.jumpTo(NoteReviewFragment.newInstance(notebookKey))
				NotebookMemoryState.infant -> {
					//todo jump to learning plan initialization page
				}
				NotebookMemoryState.paused->{
					//todo jump to learning plan paused page
				}
			}
		}
		
		tv_bookInfo.visibility = View.VISIBLE
		if (notebook is MutableNotebook) {
			b_addNote.visibility = View.VISIBLE
			b_review.visibility = View.VISIBLE
		}
		else {
			b_addNote.visibility = View.GONE
			b_review.visibility = View.GONE
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
				tv_bookInfo.visibility = View.GONE
				b_addNote.visibility = View.GONE
				b_review.visibility = View.GONE
				sv_search.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
				showingNotes = searchResult
				//todo start search thread
			} else {
				tv_bookInfo.visibility = View.VISIBLE
				if (notebook is MutableNotebook) {
					b_addNote.visibility = View.VISIBLE
					b_review.visibility = View.VISIBLE
				}
				sv_search.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
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
					//todo do search
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

class NoteItemView(context:Context):LinearLayout(context){
	private val tv_title:TextView
	private val tv_content:TextView
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
				else ->{ }
			}
			
		}
}

abstract class SectionBufferList<T>
constructor(val sectionSize:Int = 20,val sectionCount:Int = 10)
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