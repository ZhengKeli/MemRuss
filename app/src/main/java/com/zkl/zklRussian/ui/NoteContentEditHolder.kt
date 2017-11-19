package com.zkl.zklRussian.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.QuestionContent
import org.jetbrains.anko.inputMethodManager

interface NoteContentEditHolder{
	val view:View
	fun requestFocus()
	
	var noteContent: NoteContent?
	fun isCompatible(noteContent: NoteContent):Boolean
	fun applyChange(): NoteContent
}
val typedNoteContentEditHolders = hashMapOf<String,(Context, ViewGroup?)->NoteContentEditHolder>(
	QuestionContent::class.simpleName!! to ::QuestionContentEditHolder
)

class QuestionContentEditHolder(context: Context, container: ViewGroup? = null) : NoteContentEditHolder{
	
	//views
	private lateinit var et_question: EditText
	private lateinit var et_answer: EditText
	override val view: View = LayoutInflater.from(context)
		.inflate(R.layout.cv_note_content_edit_question, container,false).apply {
		et_question = findViewById(R.id.et_question) as EditText
		et_answer = findViewById(R.id.et_answer) as EditText
	}
	
	private fun updateViews() {
		et_question.setText(questionContent?.question?:"", TextView.BufferType.NORMAL)
		et_answer.setText(questionContent?.answer?:"", TextView.BufferType.NORMAL)
	}
	
	override fun requestFocus() {
		et_question.requestFocus()
		et_question.context.inputMethodManager.showSoftInput(et_question, InputMethodManager.SHOW_IMPLICIT)
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
	
	override fun applyChange()
		= QuestionContent(et_question.text.toString(), et_answer.text.toString()).also { questionContent=it }
	
}
