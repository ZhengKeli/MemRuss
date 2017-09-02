package com.zkl.ZKLRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.zkl.ZKLRussian.R
import com.zkl.ZKLRussian.control.myApp
import com.zkl.ZKLRussian.core.note.*

class NoteFragment() : Fragment() {
	
	//view
	private lateinit var ll_topBar:LinearLayout
	private lateinit var b_edit: Button
	private lateinit var b_delete: Button
	private lateinit var ll_footBar:LinearLayout
	private lateinit var b_ok: Button
	private lateinit var b_cancel: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_viewer, container, false).also { rootView ->
		
		ll_topBar = rootView.findViewById(R.id.ll_topBar) as LinearLayout
		b_edit = rootView.findViewById(R.id.b_edit) as Button
		b_delete = rootView.findViewById(R.id.b_delete) as Button
		
		ll_footBar = rootView.findViewById(R.id.ll_footBar) as LinearLayout
		b_ok = rootView.findViewById(R.id.b_ok) as Button
		b_cancel = rootView.findViewById(R.id.b_cancel) as Button
		
	}
	override fun onStart() {
		super.onStart()
		
		ll_topBar.isEnabled = notebook is MutableNotebook
		
		b_edit.setOnClickListener {
			noteContentFragment!!.beginEdit()
			mode = Mode.edit
		}
		var deleteConfirmed = false
		b_delete.setOnClickListener {
			if (!deleteConfirmed) {
				deleteConfirmed = true
				b_delete.setText(R.string.confirm)
			} else {
				deleteConfirmed = false
				b_delete.setText(R.string.delete)
				
				processDeleteNote()
				notebookActivity.jumpBackFragment()
			}
		}
		
		b_ok.setOnClickListener{
			if (mode == Mode.create) {
				mutableNotebook.addNote(noteContentFragment!!.noteContent!!)
				notebookActivity.jumpBackFragment()
			}else if (mode == Mode.edit) {
				//save to notebook
				mutableNotebook.modifyNoteContent(noteId,noteContentFragment!!.noteContent!!)
				
				//update views
				noteContentFragment!!.stopEdit(true)
				mode = Mode.view
			}
		}
		b_cancel.setOnClickListener {
			if (mode == Mode.create) {
				notebookActivity.jumpBackFragment()
			} else if (mode == Mode.edit) {
				noteContentFragment!!.stopEdit(false)
				mode = Mode.view
			}
		}
		
		//updateMode
		updateMode()
		
		//update noteContent
		val noteContent =
			if (mode == Mode.create) QuestionContent("", "")
			else note!!.content
		updateNoteContent(noteContent)
	}
	
	private var noteContentFragment:NoteContentFragment? = null
	override fun onAttachFragment(childFragment: Fragment) {
		super.onAttachFragment(childFragment)
		noteContentFragment = childFragment as? NoteContentFragment
	}
	private fun updateNoteContent(noteContent: NoteContent){
		val updateSucceed = noteContentFragment?.updateNoteContent(noteContent) == true
		if (!updateSucceed) {
			val fragment = typedNoteContentFragmentClasses[noteContent.typeTag]?.newInstance()
				?:throw RuntimeException("The noteContent type \"${noteContent.typeTag}\" is not supported.")
			childFragmentManager.beginTransaction()
				.replace(R.id.fl_noteContent_container, fragment)
				.commit()
			fragment.updateNoteContent(noteContent)
			noteContentFragment =fragment
		}
	}
	
	enum class Mode { view, edit, create }
	private var mode: Mode = Mode.view
	private fun updateMode(mode:Mode=this.mode){
		ll_topBar.visibility = if (mode==Mode.view) View.VISIBLE else View.GONE
		ll_footBar.visibility = if (mode == Mode.view) View.GONE else View.VISIBLE
		this.mode=mode
	}
	
	
	
	//key
	private var notebookKey:Int = -1
	private var noteId:Long = -1
	constructor(notebookKey:Int, noteId:Long, mode: Mode):this(){
		this.notebookKey = notebookKey
		this.noteId = noteId
		this.mode = mode
	}
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		notebookKey = savedInstanceState?.getInt(this::notebookKey.name) ?: notebookKey
		noteId = savedInstanceState?.getLong(this::noteId.name)?:noteId
		mode = savedInstanceState?.getInt(this::mode.name)?.let { Mode.values()[it] }?: mode
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putInt(this::notebookKey.name, notebookKey)
		outState.putLong(this::noteId.name, noteId)
		outState.putInt(this::mode.name, mode.ordinal)
	}
	
	
	//note
	private val _notebook: Notebook by lazy { myApp.noteManager.getRegisterNotebook(notebookKey)!! }
	private val notebook: Notebook get() = _notebook
	private val mutableNotebook: MutableNotebook get() = notebook as MutableNotebook
	private fun processDeleteNote(): Boolean
		= mutableNotebook.run { deleteNote(noteId); true }
	
	private var _note: Note? = null
	private val note: Note? get() = _note ?: noteId.takeIf { it != -1L }?.let { _notebook.getNote(it) }
	
}



abstract class NoteContentFragment:Fragment(){
	abstract val noteContent: NoteContent?
	abstract fun updateNoteContent(noteContent: NoteContent):Boolean
	
	abstract val isEditMode:Boolean
	abstract fun beginEdit()
	abstract fun stopEdit(apply:Boolean)
}
val typedNoteContentFragmentClasses = hashMapOf<String,Class<out NoteContentFragment>>(
	QuestionContent::class.simpleName!! to QuestionContentFragment::class.java
)

class QuestionContentFragment:NoteContentFragment(){
	
	//views
	private lateinit var tv_question: EditText
	private lateinit var tv_answer: EditText
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.cv_question_content,container,false).apply {
		tv_question = findViewById(R.id.tv_question) as EditText
		tv_answer = findViewById(R.id.tv_answer) as EditText
	}
	override fun onStart() {
		super.onStart()
		syncTextViews()
	}
	private fun syncTextViews(apply: Boolean=false){
		if (apply) {
			noteContent = QuestionContent(tv_question.text.toString(), tv_answer.text.toString())
		} else {
			tv_question.setText(noteContent!!.question, TextView.BufferType.NORMAL)
			tv_answer.setText(noteContent!!.answer, TextView.BufferType.NORMAL)
		}
	}
	
	
	//noteContent
	override var noteContent: QuestionContent? = null
	override fun updateNoteContent(noteContent: NoteContent): Boolean {
		this.noteContent = noteContent as? QuestionContent ?: return false
		syncTextViews()
		return true
	}
	
	
	//editMode
	override var isEditMode: Boolean = false
		private set
	override fun beginEdit() {
		tv_question.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
		tv_answer.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
		isEditMode=true
	}
	override fun stopEdit(apply: Boolean) {
		tv_question.inputType = InputType.TYPE_NULL
		tv_answer.inputType = InputType.TYPE_NULL
		syncTextViews(apply)
		isEditMode=false
	}
	
}
