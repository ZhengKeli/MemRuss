package com.zkl.ZKLRussian.control.note_old.note

abstract class NoteBody : Cloneable {
	abstract fun getClone():NoteBody

	//types
	enum class Type {
		meaning
	}
	abstract val type: Type
	/**
	 * @return 一个比例数值，表示其有多少倍的workLoad
	 */
	abstract val workLoadRate: Float
	
	//duplicate
	abstract val duplicateTags: Array<String>
	abstract fun compareWith(other: NoteBody): NoteDuplication.Similarity
	
	//search
	abstract val searchTags: Array<String>
	fun matchSearchTags(key: String): Boolean {
		for (tag in searchTags) {
			if (key == tag) {
				return true
			}
		}
		return false
	}
	
	
	//typed body
	class QuestionNoteBody(val question: String, val answer: String) : NoteBody() {
		override val type: Type
			get() = Type.meaning
		override val workLoadRate: Float
			get() = 1f


		override val duplicateTags: Array<String>
			get() = arrayOf(question)
		
		override fun compareWith(other: NoteBody): NoteDuplication.Similarity {
			if (other is QuestionNoteBody) {
				if (other.question == question) {
					if (other.answer == answer) {
						return NoteDuplication.Similarity.same
					} else {
						return NoteDuplication.Similarity.conflict
					}
				} else {
					return NoteDuplication.Similarity.different
				}
			} else {
				return NoteDuplication.Similarity.different
			}
		}
		
		override val searchTags: Array<String>
			get() = arrayOf(question, answer)


		override fun getClone(): QuestionNoteBody {
			return QuestionNoteBody(this.question, this.answer)
		}
	}
}
