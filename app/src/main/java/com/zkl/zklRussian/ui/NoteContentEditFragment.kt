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
	abstract val noteContent: NoteContent?
	abstract fun setNoteContent(noteContent: NoteContent):Boolean
	abstract fun syncTexts(apply: Boolean): NoteContent
}
val typedNoteContentEditFragments = hashMapOf<String,Class<out NoteContentEditFragment>>(
	QuestionContent::class.simpleName!! to QuestionContentEditFragment::class.java
)


class QuestionContentEditFragment : NoteContentEditFragment(){
	
	//views
	private var et_question: EditText?=null
	private var et_answer: EditText?=null
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_question_content_edit,container,false).apply {
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
		syncTexts(false)
		
		val focusEditText = if(noteContent?.question?.isEmpty() != false) et_question!! else et_answer!!
		focusEditText.requestFocus()
		(context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
			.showSoftInput(focusEditText, InputMethodManager.SHOW_IMPLICIT)
		
	}
	
	
	//noteContent
	override var noteContent: QuestionContent? = null
	override fun setNoteContent(noteContent: NoteContent): Boolean {
		this.noteContent = noteContent as? QuestionContent ?: return false
		syncTexts(false)
		return true
	}
	override fun syncTexts(apply: Boolean): NoteContent {
		if (apply) {
			noteContent = QuestionContent(et_question!!.text.toString(), et_answer!!.text.toString())
		} else {
			et_question?.setText(noteContent!!.question, TextView.BufferType.NORMAL)
			et_answer?.setText(noteContent!!.answer, TextView.BufferType.NORMAL)
		}
		return noteContent!!
	}
	
}



