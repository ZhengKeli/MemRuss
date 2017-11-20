package com.zkl.zklRussian.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.core.note.Note
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.QuestionContent

interface NoteContentItemHolder {
	val view:View
	var noteContent: NoteContent?
	fun isCompatible(noteContent: NoteContent):Boolean
}

abstract class NoteListAdapter:BaseAdapter(){
	abstract override fun getCount() :Int
	abstract override fun getItem(position: Int):Note
	override fun getItemId(position: Int): Long = 0L
	
	abstract val context:Context
	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val noteContent = getItem(position).content
		val oldHolder = convertView?.tag as? NoteContentItemHolder
		if (oldHolder?.isCompatible(noteContent) == true) {
			oldHolder.noteContent = noteContent
			return oldHolder.view
		}else{
			val holder = typedNoteContentItemHolders[noteContent.typeTag]?.invoke(context, parent)
				?: throw RuntimeException("The noteContent type \"${noteContent.typeTag}\" is not supported.")
			holder.noteContent = noteContent
			holder.view.tag = holder
			return holder.view
		}
	}
}



val typedNoteContentItemHolders = hashMapOf<String,(Context, ViewGroup?)->NoteContentItemHolder>(
	QuestionContent::class.simpleName!! to ::QuestionContentItemHolder
)

class QuestionContentItemHolder(context: Context, container: ViewGroup? = null) : NoteContentItemHolder {
	
	//views
	private lateinit var tv_question: TextView
	private lateinit var tv_answer: TextView
	private lateinit var cb_selected:CheckBox
	override val view: View = LayoutInflater.from(context)
		.inflate(R.layout.cv_note_content_item_question, container,false).apply {
		tv_question = findViewById(R.id.tv_question) as TextView
		tv_answer = findViewById(R.id.tv_answer) as TextView
		cb_selected = findViewById(R.id.cb_selected) as CheckBox
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

