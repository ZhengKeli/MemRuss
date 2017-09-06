package com.zkl.ZKLRussian.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zkl.ZKLRussian.R
import com.zkl.ZKLRussian.core.note.MutableNotebook
import com.zkl.ZKLRussian.core.note.Note
import com.zkl.ZKLRussian.core.note.QuestionContent
import java.util.*

class NotebookFragment : NotebookHoldingFragment {
	constructor() : super()
	constructor(notebookKey: Int) : super(notebookKey)
	
	//views
	private lateinit var tv_bookName: TextView
	private lateinit var b_menu:Button
	private lateinit var tv_bookInfo:TextView
	private lateinit var b_addNote:ImageButton
	private lateinit var sv_search:SearchView
	private lateinit var lv_notes:ListView
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook, container, false).also { rootView ->
		
		tv_bookName = rootView.findViewById(R.id.tv_bookName) as TextView
		b_menu = rootView.findViewById(R.id.b_menu) as Button
		tv_bookInfo = rootView.findViewById(R.id.tv_bookInfo) as TextView
		b_addNote = rootView.findViewById(R.id.b_addNote) as ImageButton
		sv_search = rootView.findViewById(R.id.sv_search) as SearchView
		lv_notes = rootView.findViewById(R.id.lv_notes) as ListView
		
	}
	override fun onStart() {
		super.onStart()
		
		tv_bookName.text = notebook.name
		//todo make it string resources of android
		tv_bookInfo.text = "共${notebook.noteCount}个词条"
		
		b_menu.setOnClickListener {
			Toast.makeText(context,"还没有菜单！",Toast.LENGTH_SHORT).show()
			//todo show menu
		}
		
		
		
		if(notebook !is MutableNotebook)
			b_addNote.visibility = View.GONE
		else
			b_addNote.setOnClickListener {
				notebookActivity.jumpToFragment(NoteEditFragment(notebookKey, -1),true)
			}
		
		
		//todo search
		sv_search.setOnSearchClickListener {
			tv_bookInfo.visibility = View.GONE
			b_addNote.visibility = View.GONE
		}
		sv_search.setOnCloseListener {
			tv_bookInfo.visibility = View.VISIBLE
			b_addNote.visibility = View.VISIBLE
			false
		}
		
		//todo show notes
		showNotes()
		lv_notes.adapter = object :BaseAdapter(){
			override fun getItem(position: Int) = showingNotes!![getItemId(position).toInt()]
			override fun getItemId(position: Int) = position.toLong()
			
			override fun getCount() = showingNotes?.size?:0
			override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
				val view = (convertView as? NoteItemView)?: NoteItemView(activity)
				view.note = getItem(position)
				return view
			}
		}
		lv_notes.setOnItemClickListener { _, view, _, _ ->
			view as NoteItemView
			activity.supportFragmentManager.beginTransaction()
				.replace(NoteViewFragment(notebookKey, view.note!!.id))
				.addToBackStack(null)
				.commit()
		}
	}
	
	
	//notes
	var showingNotes:List<Note>? = null
	private fun showNotes(){
		showingNotes = object : SectionBufferList<Note>(){
			override fun getSection(startFrom: Int): List<Note>
				= notebook.selectLatestNotes(sectionSize, startFrom)
			override val size: Int = notebook.noteCount
		}
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
				else ->{
				
				}
			}
			
		}
}

abstract class SectionBufferList<T>
constructor(val sectionSize:Int = 50,val sectionCount:Int = 10)
	: AbstractList<T>() {
	
	private val sections = LinkedList<List<T>>()
	private var bufferFrom:Int = 0
	private val bufferSize:Int get()= sectionSize*sections.size
	@Synchronized override fun get(index: Int): T {
		val relative = index - bufferFrom
		while (relative < 0) appendFirst()
		while (relative>= bufferSize) appendLast()
		return sections[relative / sectionSize][relative % sectionSize]
	}
	@Synchronized private fun appendFirst(){
		bufferFrom -= sectionSize
		sections.addFirst(getSection(bufferFrom))
		if (sections.size > sectionCount) sections.removeLast()
	}
	@Synchronized private fun appendLast(){
		sections.addLast(getSection(bufferFrom + bufferSize))
		if (sections.size > sectionCount) {
			sections.removeFirst()
			bufferFrom += sectionSize
		}
	}
	
	abstract fun getSection(startFrom:Int):List<T>
	
}