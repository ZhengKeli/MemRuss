package com.zkl.memruss.core.note

import com.zkl.memruss.core.note.base.NoteConflictException
import com.zkl.memruss.core.note.base.NoteMemoryState

data class ConflictSolution(val coverContent: Boolean, val coverProgress: Boolean)
typealias NoteConflictSolver = (conflictNoteId: Long, newNote: Note) -> ConflictSolution
typealias ContentConflictSolver = (conflictNoteId: Long, newContent: NoteContent) -> ConflictSolution

/**
 * 添加一个词条
 * @return 返回刚加入的词条的 noteId
 */
fun MutableNotebook.addNote(content: NoteContent, conflictSolver: ContentConflictSolver): Long {
	return try {
		addNote(content)
	} catch (e: NoteConflictException) {
		val conflictId = e.conflictedNoteId
		val solution = conflictSolver(conflictId, content)
		if (solution.coverContent)
			modifyNoteContent(conflictId, content)
		if (solution.coverProgress)
			modifyNoteMemory(conflictId, NoteMemoryState.infantState())
		conflictId
	}
}

/**
 * 添加一堆词条
 * @return 返回刚加入的词条的 noteId
 */
fun MutableNotebook.addNotes(contents: Collection<NoteContent>, conflictSolver: ContentConflictSolver)
	= contents.forEach { addNote(it, conflictSolver) }

/**
 * 完全保留地添加一个词条
 */
fun MutableNotebook.rawAddNote(note: Note, conflictSolver: NoteConflictSolver): Long {
	return try {
		rawAddNote(note)
	} catch (e: NoteConflictException) {
		val conflictId = e.conflictedNoteId
		val solution = conflictSolver(conflictId, note)
		if (solution.coverContent) modifyNoteContent(conflictId, note.content)
		if (solution.coverProgress) modifyNoteMemory(conflictId, note.memoryState)
		conflictId
	}
}

/**
 * 完全保留地添加一堆词条
 */
fun MutableNotebook.rawAddNotes(contents: Collection<Note>, conflictSolver: NoteConflictSolver)
	= contents.forEach { rawAddNote(it, conflictSolver) }

/**
 * 修改 note 的内容
 */
fun MutableNotebook.modifyNoteContent(noteId: Long, content: NoteContent, conflictSolver: ContentConflictSolver) {
	try {
		modifyNoteContent(noteId, content)
	} catch (e: NoteConflictException) {
		val conflictId = e.conflictedNoteId
		val solution = conflictSolver(conflictId, content)
		if (solution.coverContent) {
			//如果 coverContent 就会无视 coverProgress
			deleteNote(conflictId)
			modifyNoteContent(noteId, content)
		} else if (solution.coverProgress) {
			modifyNoteMemory(noteId, NoteMemoryState.infantState())
		}
	}
}
