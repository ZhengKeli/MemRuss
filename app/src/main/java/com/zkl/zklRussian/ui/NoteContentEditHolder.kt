package com.zkl.zklRussian.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.QuestionContent
import kotlinx.android.synthetic.main.cv_note_content_edit_question.view.*
import org.jetbrains.anko.inputMethodManager

interface NoteContentEditHolder {
	val view: View
	fun requestFocus()
	
	var noteContent: NoteContent?
	fun isCompatible(noteContent: NoteContent): Boolean
	fun applyChange(): NoteContent
}

val typedNoteContentEditHolders = hashMapOf<String, (Context, ViewGroup?) -> NoteContentEditHolder>(
	QuestionContent::class.simpleName!! to ::QuestionContentEditHolder
)

class QuestionContentEditHolder(context: Context, container: ViewGroup? = null) : NoteContentEditHolder {
	
	//views
	override val view: View = LayoutInflater.from(context)
		.inflate(R.layout.cv_note_content_edit_question, container, false)
	
	private fun updateViews() = view.run {
		view.et_question.setText(questionContent?.question ?: "", TextView.BufferType.NORMAL)
		view.et_answer.setText(questionContent?.answer ?: "", TextView.BufferType.NORMAL)
	}
	
	override fun requestFocus() = view.run {
		et_question.requestFocus()
		et_question.context.inputMethodManager
			.showSoftInput(et_question, InputMethodManager.SHOW_IMPLICIT)
		Unit
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
		= view.run { QuestionContent(et_question.text.toString(), et_answer.text.toString()) }.also { questionContent = it }
	
}
