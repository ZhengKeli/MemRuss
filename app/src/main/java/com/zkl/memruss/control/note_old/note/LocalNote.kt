package com.zkl.memruss.control.note_old.note



abstract class LocalNote(
	val id: Long = -1,
	var modifyTime: Long = -1,
	val noteBody: NoteBody,
	noteProgress: Memory.NoteProgress? = null)  {
	
	val createTime: Long get() = modifyTime
	
	val noteProgress: Memory.NoteProgress = noteProgress ?: Memory.getNullProgress()
	
	
	val nextTime: Long get() = noteProgress.nextTime
	val progress: Int get() = noteProgress.progress
	val isPlanned: Boolean get() = noteProgress.isPlanned
	val isLearning: Boolean get() = noteProgress.isLearning
	
	abstract fun getClone(
		id: Long = this.id, modifyTime: Long = this.modifyTime,
		noteProgress: Memory.NoteProgress? = this.noteProgress): LocalNote
	
}

class LocalQuestionNote(
	id: Long,
	modifyTime: Long,
	createTime: Long,
	noteBody: NoteBody.QuestionNoteBody,
	noteProgress: Memory.NoteProgress?
) : LocalNote(id, modifyTime, noteBody, noteProgress) {
	
	
	val question: String = noteBody.question
	val answer: String = noteBody.answer
	
	override fun getClone(id: Long, modifyTime: Long,
	                      noteProgress: Memory.NoteProgress?): LocalQuestionNote
		= LocalQuestionNote(id, modifyTime,createTime, NoteBody.QuestionNoteBody(question, answer), noteProgress)
	
}