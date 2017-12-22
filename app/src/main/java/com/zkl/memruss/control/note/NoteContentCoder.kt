package com.zkl.memruss.control.note

import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.QuestionContent
import com.zkl.memruss.core.note.base.InternalNotebookException
import org.json.JSONObject

interface NoteContentCoder {
	fun encode(content: NoteContent): String
	fun decode(string: String): NoteContent
}

val noteContentCoders:Map<String,NoteContentCoder> = hashMapOf(
	QuestionContent::class.simpleName!! to object :NoteContentCoder{
		override fun encode(content: NoteContent): String {
			if (content !is QuestionContent)
				throw InternalNotebookException("This coder can only apply for QuestionContent, not ${content.javaClass.simpleName}")
			return content.run {
				JSONObject(mapOf(
					this::question.name to question,
					this::answer.name to answer)
				).toString()
			}
		}
		override fun decode(string: String) = JSONObject(string).run {
			QuestionContent(
				getString(QuestionContent::question.name),
				getString(QuestionContent::answer.name)
			)
		}
	}
)


