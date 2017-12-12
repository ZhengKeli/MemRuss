package com.zkl.zklRussian.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.zklRussian.R
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.QuestionContent
import com.zkl.zklRussian.core.note.base.NoteMemoryState
import com.zkl.zklRussian.core.note.base.getNextReviewTime
import kotlinx.android.synthetic.main.cv_note_content_review_question.view.*

interface NoteContentReviewHolder {
	val view: View
	var noteContent: NoteContent?
	fun isCompatible(noteContent: NoteContent): Boolean
	var onResultListener: ((ReviewResult) -> Unit)?
}

interface ReviewResult {
	fun updateNoteMemory(oldMemoryState: NoteMemoryState, nowTime: Long = System.currentTimeMillis()): NoteMemoryState
	
	companion object {
		val standardIncrease = object : ReviewResult {
			override fun updateNoteMemory(oldMemoryState: NoteMemoryState, nowTime: Long)
				= oldMemoryState.getNextReviewTime(true, nowTime)
		}
		val standardDegrease = object : ReviewResult {
			override fun updateNoteMemory(oldMemoryState: NoteMemoryState, nowTime: Long)
				= oldMemoryState.getNextReviewTime(false, nowTime)
		}
	}
}

val typedNoteContentReviewHolders = hashMapOf<String, (Context, ViewGroup?) -> NoteContentReviewHolder>(
	QuestionContent::class.simpleName!! to ::QuestionContentReviewHolder
)

class QuestionContentReviewHolder(context: Context, container: ViewGroup? = null) : NoteContentReviewHolder {
	
	//views
	override val view: View = LayoutInflater.from(context)
		.inflate(R.layout.cv_note_content_review_question, container, false).apply {
		b_show.setOnClickListener {
			b_show.visibility = View.GONE
			tv_answer.visibility = View.VISIBLE
			cl_resultBar.visibility = View.VISIBLE
		}
		b_remembered.setOnClickListener {
			onResultListener?.invoke(ReviewResult.standardIncrease)
		}
		b_forgot.setOnClickListener {
			onResultListener?.invoke(ReviewResult.standardDegrease)
		}
	}
	
	private fun updateViews() = view.run {
		tv_question.text = questionContent?.question ?: ""
		tv_answer.text = questionContent?.answer ?: ""
		
		b_show.visibility = View.VISIBLE
		tv_answer.visibility = View.GONE
		cl_resultBar.visibility = View.INVISIBLE
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
	
	override var onResultListener: ((reviewResult: ReviewResult) -> Unit)? = null
	
}
