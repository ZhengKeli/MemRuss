package com.zkl.memruss.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.memruss.R
import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.QuestionContent
import com.zkl.memruss.core.note.base.NoteTypeNotSupportedException
import kotlinx.android.synthetic.main.cv_note_content_view_question.view.*

interface NoteContentViewHolder {
	val view: View
	var noteContent: NoteContent?
	fun isCompatible(noteContent: NoteContent): Boolean
}


//typed holders map

private val typedNoteContentViewHolders = hashMapOf<String, (Context, ViewGroup?) -> NoteContentViewHolder>(
	QuestionContent::class.simpleName!! to ::QuestionContentViewHolder
)

fun NoteContent.newViewHolder(context: Context, container: ViewGroup? = null)
	= typedNoteContentViewHolders[typeTag]?.invoke(context, container)?.also { it.noteContent=this }

fun NoteContent.newViewHolderOrThrow(context: Context, container: ViewGroup? = null)
	= newViewHolder(context, container) ?: throw NoteTypeNotSupportedException(typeTag)


//typed holders class

class QuestionContentViewHolder(context: Context, container: ViewGroup? = null) : NoteContentViewHolder {
	
	//views
	override val view: View = LayoutInflater.from(context)
		.inflate(R.layout.cv_note_content_view_question, container, false)
	
	private fun updateViews() = view.run {
		tv_question.text = questionContent?.question ?: ""
		tv_answer.text = questionContent?.answer ?: ""
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
