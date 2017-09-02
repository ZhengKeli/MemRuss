package com.zkl.ZKLRussian.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zkl.ZKLRussian.R
import com.zkl.ZKLRussian.control.myApp
import com.zkl.ZKLRussian.core.note.MutableNotebook
import com.zkl.ZKLRussian.core.note.Note
import com.zkl.ZKLRussian.core.note.Notebook
import com.zkl.ZKLRussian.core.note.QuestionContent

class NotebookFragment() : Fragment() {
	
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
				notebookActivity.jumpToFragment(NoteCreationFragment(notebookKey),true)
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
				.replace(NoteFragment(notebookKey, view.note!!.id,ViewMode.view))
				.addToBackStack(null)
				.commit()
		}
	}
	
	
	//key
	private var notebookKey:Int = -1
	constructor(notebookKey:Int):this(){
		this.notebookKey = notebookKey
	}
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		notebookKey = savedInstanceState?.getInt(this::notebookKey.name) ?: notebookKey
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putInt(this::notebookKey.name, notebookKey)
	}
	
	
	//notebook
	private val _notebook: Notebook by lazy { myApp.noteManager.getRegisterNotebook(notebookKey)!! }
	val notebook: Notebook get() = _notebook
	val mutableNotebook: MutableNotebook get() = notebook as MutableNotebook
	
	
	//notes
	var showingNotes:List<Note>? = null
	private fun showNotes(){
		showingNotes = object :SectionBufferList<Note>(){
			override fun getSection(startFrom: Int): List<Note>
				= notebook.selectLatestNotes(sectionSize, startFrom)
			override val size: Int = _notebook.noteCount
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

