package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.NoteMemoryState
import com.zkl.zklRussian.core.note.QuestionContent


class NoteViewFragment : NoteHoldingFragment {
	
	constructor():super()
	constructor(notebookPath: NotebookKey, noteId: Long) : super(notebookPath,noteId)
	
	//view
	private lateinit var tv_title: TextView
	private lateinit var tv_info: TextView
	private lateinit var b_edit: Button
	private lateinit var b_delete: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_view, container, false).apply {
		tv_title = findViewById(R.id.tv_title) as TextView
		tv_info = findViewById(R.id.tv_info) as TextView
		b_edit = findViewById(R.id.b_edit) as Button
		b_delete = findViewById(R.id.b_delete) as Button
	}
	override fun onStart() {
		super.onStart()
		
		tv_title.text = getString(R.string.Note_view_id, noteId)
		if (note.memory.state != NoteMemoryState.infant) {
			tv_info.text = """
				memoryProgress:${note.memory.progress}
				sumLoad:${note.memory.load}
				reviewTime:${note.memory.reviewTime}
			""".trimMargin()
		} else {
			tv_info.visibility = View.GONE
		}
		
		b_edit.isEnabled = notebook is MutableNotebook
		b_edit.setOnClickListener {
			mainActivity.jumpToFragment(NoteEditFragment(notebookKey,noteId),true)
		}
		
		b_delete.isEnabled = notebook is MutableNotebook
		b_delete.setOnClickListener {
			NoteDeleteDialog(notebookKey, noteId).show(fragmentManager, null)
		}
		
		updateNoteContent(note.content)
		
	}
	
	
	//noteContent
	private var noteContentViewFragment: NoteContentViewFragment? = null
	override fun onAttachFragment(childFragment: Fragment) {
		super.onAttachFragment(childFragment)
		noteContentViewFragment = childFragment as? NoteContentViewFragment
	}
	private fun updateNoteContent(noteContent: NoteContent){
		val updateSucceed = noteContentViewFragment?.setNoteContent(noteContent) == true
		if (!updateSucceed) {
			val fragment = typedNoteContentViewFragments[noteContent.typeTag]?.newInstance()
				?:throw RuntimeException("The noteContent type \"${noteContent.typeTag}\" is not supported.")
			childFragmentManager.beginTransaction()
				.replace(R.id.fl_noteContent_container, fragment)
				.commit()
			fragment.setNoteContent(noteContent)
			noteContentViewFragment =fragment
		}
	}
	
	
}


//ContentViewFragment
class QuestionContentViewFragment : NoteContentViewFragment(){
	
	//views
	private var tv_question: TextView?=null
	private var tv_answer: TextView?=null
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_question_content_view,container,false).apply {
		tv_question = findViewById(R.id.tv_question) as TextView
		tv_answer = findViewById(R.id.tv_answer) as TextView
	}
	override fun onDestroyView() {
		super.onDestroyView()
		tv_question=null
		tv_answer=null
	}
	override fun onStart() {
		super.onStart()
		updateTextViews()
	}
	
	private fun updateTextViews(){
		tv_question?.text = noteContent!!.question
		tv_answer?.text = noteContent!!.answer
	}
	
	
	//noteContent
	override var noteContent: QuestionContent? = null
	override fun setNoteContent(noteContent: NoteContent): Boolean {
		this.noteContent = noteContent as? QuestionContent ?: return false
		updateTextViews()
		return true
	}
	
}

abstract class NoteContentViewFragment :Fragment(){
	abstract val noteContent: NoteContent?
	abstract fun setNoteContent(noteContent: NoteContent):Boolean
}
val typedNoteContentViewFragments = hashMapOf<String,Class<out NoteContentViewFragment>>(
	QuestionContent::class.simpleName!! to QuestionContentViewFragment::class.java
)

