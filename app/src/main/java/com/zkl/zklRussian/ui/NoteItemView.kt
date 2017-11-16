package com.zkl.zklRussian.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.core.note.Note
import com.zkl.zklRussian.core.note.QuestionContent

class NoteItemView(context: Context) : LinearLayout(context) {
	private val tv_title: TextView
	private val tv_content: TextView
	
	init {
		LayoutInflater.from(context).inflate(R.layout.adapter_note_item, this, true)
		tv_title = this.findViewById(R.id.tv_title) as TextView
		tv_content = this.findViewById(R.id.tv_content) as TextView
	}
	
	var note: Note? = null
		set(value) {
			field = value
			if (value == null) return
			
			val content = value.content
			when (content) {
				is QuestionContent -> {
					this.post {
						tv_title.text = content.question
						tv_content.text = content.answer
					}
				}
				else -> {
				}
			}
			
		}
}