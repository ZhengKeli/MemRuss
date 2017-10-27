package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.QuestionContent

//ContentViewFragment
abstract class NoteContentViewFragment : Fragment(){
	abstract val noteContent: NoteContent?
	abstract fun setNoteContent(noteContent: NoteContent):Boolean
}
val typedNoteContentViewFragments = hashMapOf<String,Class<out NoteContentViewFragment>>(
	QuestionContent::class.simpleName!! to QuestionContentViewFragment::class.java
)


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


