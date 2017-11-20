package com.zkl.zklRussian.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.QuestionContent

interface NoteContentViewHolder {
	val view:View
	var noteContent: NoteContent?
	fun isCompatible(noteContent: NoteContent):Boolean
}



val typedNoteContentViewHolders = hashMapOf<String,(Context, ViewGroup?)->NoteContentViewHolder>(
	QuestionContent::class.simpleName!! to ::QuestionContentViewHolder
)

class QuestionContentViewHolder(context: Context, container: ViewGroup? = null) : NoteContentViewHolder {
	
	//views
	private lateinit var tv_question: TextView
	private lateinit var tv_answer: TextView
	override val view: View = LayoutInflater.from(context)
		.inflate(R.layout.cv_note_content_view_question, container,false).apply {
		tv_question = findViewById(R.id.tv_question) as TextView
		tv_answer = findViewById(R.id.tv_answer) as TextView
	}
	
	private fun updateViews() {
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
