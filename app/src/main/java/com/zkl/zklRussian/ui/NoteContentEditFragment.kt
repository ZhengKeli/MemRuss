package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.QuestionContent

abstract class NoteContentEditFragment : Fragment(){
	abstract var noteContent: NoteContent?
	abstract fun isCompatible(noteContent: NoteContent):Boolean
	abstract fun applyChange(): NoteContent
}
val typedNoteContentEditFragments = hashMapOf<String,Class<out NoteContentEditFragment>>(
	QuestionContent::class.simpleName!! to QuestionContentEditFragment::class.java
)


class QuestionContentEditFragment : NoteContentEditFragment(){
	
	//views
	private var et_question: EditText? = null
	private var et_answer: EditText? = null
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_content_edit_question,container,false).apply {
		et_question = findViewById(R.id.et_question) as EditText
		et_answer = findViewById(R.id.et_answer) as EditText
	}
	override fun onDestroyView() {
		super.onDestroyView()
		et_question = null
		et_answer = null
	}
	override fun onStart() {
		super.onStart()
		updateViews()
		
		val focusEditText = if(questionContent?.question?.isEmpty() != false) et_question!! else et_answer!!
		focusEditText.requestFocus()
		(context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
			.showSoftInput(focusEditText, InputMethodManager.SHOW_IMPLICIT)
		
	}
	
	private fun updateViews() {
		et_question?.setText(questionContent?.question?:"", TextView.BufferType.NORMAL)
		et_answer?.setText(questionContent?.answer?:"", TextView.BufferType.NORMAL)
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
			questionContent = value as QuestionContent
		}
	
	override fun isCompatible(noteContent: NoteContent)
		= noteContent is QuestionContent
	
	override fun applyChange():QuestionContent {
		return QuestionContent(et_question!!.text.toString(), et_answer!!.text.toString()).also { questionContent=it }
	}
	
}



