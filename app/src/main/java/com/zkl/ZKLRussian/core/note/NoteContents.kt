package com.zkl.ZKLRussian.core.note


data class QuestionContent(val question: String, val answer: String) : NoteContent {
	override val searchTags: Collection<String> get() = arrayListOf(question, answer)
	override val untypedUniqueTags: Collection<String> get() = arrayListOf(question)
}


