package com.zkl.zklRussian.core.note

import com.zkl.zklRussian.core.note.base.NoteConflictException
import com.zkl.zklRussian.core.note.base.NoteMemoryState
import com.zkl.zklRussian.core.note.base.NoteMemoryStatus

typealias ConflictSolver = (newContent: NoteContent, conflictNoteId: Long) -> ConflictSolution

data class ConflictSolution(val override: Boolean, val resetProgress: Boolean)


/**
 * 添加一个词条
 * @return 返回刚加入的词条的 noteId
 */
fun MutableNotebook.addNote(content: NoteContent, conflictSolver: ConflictSolver): Long {
	return try {
		addNote(content)
	} catch (e: NoteConflictException) {
		val conflictId = e.conflictedNoteId
		val solution = conflictSolver(content, conflictId)
		if (solution.override)
			modifyNoteContent(conflictId, content)
		if (solution.resetProgress)
			modifyNoteMemory(conflictId, NoteMemoryState.infantState())
		conflictId
	}
}

/**
 * 添加一堆词条
 * @return 返回刚加入的词条的 noteId
 */
fun MutableNotebook.addNotes(contents: Collection<NoteContent>, conflictSolver: ConflictSolver)
	= contents.forEach { addNote(it, conflictSolver) }

/**
 * 完全保留地添加一个词条
 */
fun MutableNotebook.rawAddNote(note: Note, conflictSolver: ConflictSolver): Long {
	return try {
		rawAddNote(note)
	} catch (e: NoteConflictException) {
		val conflictId = e.conflictedNoteId
		val solution = conflictSolver(note.content, conflictId)
		if (solution.override) {
			modifyNoteContent(conflictId, note.content)
			//todo 仔细考虑两个note都有进度的情况
			if (solution.resetProgress) {
				modifyNoteMemory(conflictId, NoteMemoryState.infantState())
			} else if (note.memoryState.status != NoteMemoryStatus.infant) {
				modifyNoteMemory(conflictId, note.memoryState)
			}
		} else if (solution.resetProgress) {
			modifyNoteMemory(conflictId, NoteMemoryState.infantState())
		}
		conflictId
	}
}

/**
 * 完全保留地添加一堆词条
 */
fun MutableNotebook.rawAddNotes(contents: Collection<Note>, conflictSolver: ConflictSolver)
	= contents.forEach { rawAddNote(it, conflictSolver) }

/**
 * 修改 note 的内容
 */
fun MutableNotebook.modifyNoteContent(noteId: Long, content: NoteContent, conflictSolver: ConflictSolver) {
	try {
		modifyNoteContent(noteId, content)
	} catch (e: NoteConflictException) {
		val conflictId = e.conflictedNoteId
		val solution = conflictSolver(content, conflictId)
		if (solution.override) {
			//如果 override 就会无视 resetProgress
			deleteNote(conflictId)
			modifyNoteContent(noteId, content)
		}
		else if (solution.resetProgress) {
			modifyNoteMemory(noteId, NoteMemoryState.infantState())
		}
	}
}
