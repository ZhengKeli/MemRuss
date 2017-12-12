package com.zkl.zklRussian.core.note.base

import com.zkl.zklRussian.core.note.NoteContent

interface UniqueNotebook<Note : UniqueNote<*>> : BaseNotebook<Note> {
	
	/**
	 * 搜索是否存在某个 uniqueTag
	 */
	fun checkUniqueTag(uniqueTag: String, exceptId: Long = -1L): Long
	
	/**
	 * 批量搜索是否存在某些 uniqueTag
	 */
	fun checkUniqueTags(uniqueTags: Collection<String>, exceptId: Long = -1L): Boolean {
		uniqueTags.forEach { uniqueTag ->
			val id = checkUniqueTag(uniqueTag, exceptId)
			if (id != -1L) return true
		}
		return false
	}
	
}

interface MutableUniqueNotebook<Content : UniqueContent, Note : UniqueNote<Content>>
	: MutableBaseNotebook<Content, Note>, UniqueNotebook<Note> {
	
	/**
	 * 添加一个词条
	 * @return 返回刚加入的词条的 noteId
	 */
	fun addNote(content: NoteContent, conflictSolver: ConflictSolver<Content, Note>): Long
	
	/**
	 * 添加一堆词条
	 * @return 返回刚加入的词条的 noteId
	 */
	fun addNotes(contents: Collection<NoteContent>, conflictSolver: ConflictSolver<Content, Note>)
		= contents.forEach { addNote(it, conflictSolver) }
	
}

interface UniqueNote<out Content : UniqueContent> : BaseNote<Content>

interface UniqueContent : BaseContent {
	
	/**
	 * 用于辨识重复的标签
	 * 在同一个[UniqueNotebook]里不允许有两个词条含有相同的 uniqueTag
	 */
	val uniqueTags: Collection<String>
		get() = untypedUniqueTags.map { typeTag + ":" + it }
	
	/**
	 * 不带类型信息的重复辨识标签
	 */
	val untypedUniqueTags: Collection<String>
	
}


interface ConflictSolver<Content : UniqueContent, in Note : UniqueNote<Content>> {
	fun onConflict(newContent: Content, oldNote: Note): ConflictSolution
}

data class ConflictSolution(val override: Boolean, val ridProgress: Boolean)


/**
 * 对[MutableUniqueNotebook]做操作的时候，
 * 出现了词条重复的冲突时，抛出此错误
 */
class ConflictException(
	val uniqueTag: String,
	val conflictedNoteId: Long,
	message: String = "The adding UniqueDraft with uniqueTag $uniqueTag duplicated with " +
		"another existed note with id $conflictedNoteId"
) : NotebookException(message)
