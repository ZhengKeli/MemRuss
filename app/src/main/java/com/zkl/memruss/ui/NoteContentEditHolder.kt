package com.zkl.memruss.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zkl.memruss.R
import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.QuestionContent
import com.zkl.memruss.core.note.base.NoteTypeNotSupportedException
import kotlinx.android.synthetic.main.cv_note_content_edit_question.view.*
import org.jetbrains.anko.toast

interface NoteContentEditHolder {
	val view: View
	fun requestFocus()
	
	var noteContent: NoteContent?
	fun isCompatible(noteContent: NoteContent): Boolean
	
	fun checkLegalAndAlert(): Boolean
	fun applyChange(): NoteContent
}


//typed holders map

private val typedNoteContentEditHolders = hashMapOf<String, (Context, ViewGroup?) -> NoteContentEditHolder>(
	QuestionContent::class.simpleName!! to ::QuestionContentEditHolder
)

fun NoteContent.newEditHolder(context: Context, container: ViewGroup? = null)
	= typedNoteContentEditHolders[typeTag]?.invoke(context, container)?.also { it.noteContent=this }

fun NoteContent.newEditHolderOrThrow(context: Context, container: ViewGroup? = null)
	= newEditHolder(context, container) ?: throw NoteTypeNotSupportedException(typeTag)


//typed holders class

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
		et_question.showSoftInput()
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
	
	override fun checkLegalAndAlert(): Boolean {
		view.run {
			if (et_question.text.isBlank()) {
				context.toast(R.string.question_should_not_be_blank)
				return false
			}
			if (et_answer.text.isBlank()) {
				context.toast(R.string.answer_should_not_be_blank)
				return false
			}
			return true
		}
	}
	
	override fun applyChange()
		= view.run { QuestionContent(et_question.text.toString(), et_answer.text.toString()) }.also { questionContent = it }
	
}
