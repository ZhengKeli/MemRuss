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
	abstract var noteContent: NoteContent?
	abstract fun isCompatible(noteContent: NoteContent):Boolean
}
val typedNoteContentViewFragments = hashMapOf<String,Class<out NoteContentViewFragment>>(
	QuestionContent::class.simpleName!! to QuestionContentViewFragment::class.java
)


class QuestionContentViewFragment : NoteContentViewFragment(){
	
	//views
	private var tv_question: TextView?=null
	private var tv_answer: TextView?=null
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_content_view_question,container,false).apply {
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
		updateViews()
	}
	
	private fun updateViews() {
		tv_question?.text = questionContent?.question ?: ""
		tv_answer?.text = questionContent?.answer ?: ""
	}
	
	
	//noteContent
	private var questionContent: QuestionContent? = null
		set(value) {
			field = value
			updateViews()
		}
	override var noteContent: NoteContent?
		get() = questionContent
		set(value) {
			this.questionContent = value as QuestionContent
		}
	
	override fun isCompatible(noteContent: NoteContent)
		= noteContent is QuestionContent
	
}


