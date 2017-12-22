package com.zkl.memruss.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.zkl.memruss.R
import com.zkl.memruss.core.note.Note
import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.QuestionContent
import com.zkl.memruss.core.note.base.NoteTypeNotSupportedException
import kotlinx.android.synthetic.main.cv_note_content_item_question.view.*

interface NoteContentItemHolder {
	val view: View
	var noteContent: NoteContent?
	fun isCompatible(noteContent: NoteContent): Boolean
}

abstract class NoteListAdapter : BaseAdapter() {
	abstract override fun getCount(): Int
	abstract override fun getItem(position: Int): Note
	override fun getItemId(position: Int): Long = 0L
	
	abstract val context: Context
	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val noteContent = getItem(position).content
		val oldHolder = convertView?.tag as? NoteContentItemHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
			return oldHolder.view
		} else {
			val holder = noteContent.newItemHolderOrThrow(context, parent)
			return holder.view.also { it.tag=holder }
		}
	}
}


//typed holders map

private val typedNoteContentItemHolders = hashMapOf<String, (Context, ViewGroup?) -> NoteContentItemHolder>(
	QuestionContent::class.simpleName!! to ::QuestionContentItemHolder
)

fun NoteContent.newItemHolder(context: Context, container: ViewGroup? = null)
	= typedNoteContentItemHolders[typeTag]?.invoke(context, container)?.also { it.noteContent=this }

fun NoteContent.newItemHolderOrThrow(context: Context, container: ViewGroup? = null)
	= newItemHolder(context, container) ?: throw NoteTypeNotSupportedException(typeTag)


//typed holders class

class QuestionContentItemHolder(context: Context, container: ViewGroup? = null) : NoteContentItemHolder {
	
	//views
	override val view: View = LayoutInflater.from(context)
		.inflate(R.layout.cv_note_content_item_question, container, false)
	
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

