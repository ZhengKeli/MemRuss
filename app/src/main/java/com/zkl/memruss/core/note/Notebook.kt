package com.zkl.memruss.core.note

import com.zkl.memruss.core.note.base.*
import java.io.Serializable

interface Notebook :
	BaseNotebook<Note>,
	MemoryNotebook<Note>,
	SearchableNotebook<Note>,
	UniqueNotebook<Note>

interface MutableNotebook : Notebook,
	MutableBaseNotebook<NoteContent, Note>,
	MutableMemoryNotebook<NoteContent, Note>,
	MutableUniqueNotebook<NoteContent, Note>

interface Note :
	BaseNote<NoteContent>,
	MemoryNote<NoteContent>,
	SearchableNote<NoteContent>,
	UniqueNote<NoteContent>

interface NoteContent :
	Serializable,
	BaseContent,
	SearchableContent,
	UniqueContent

data class InstantNote(
	override val id: Long,
	override val createTime: Long,
	override val content: NoteContent,
	override val contentUpdateTime: Long,
	override val memoryState: NoteMemoryState,
	override val memoryUpdateTime: Long
) : Note {
	constructor(
		note: Note,
		id: Long = note.id,
		createTime: Long = note.createTime,
		content: NoteContent = note.content,
		contentUpdateTime: Long = note.contentUpdateTime,
		memoryState: NoteMemoryState = note.memoryState,
		memoryUpdateTime: Long = note.memoryUpdateTime
	) : this(id, createTime, content, contentUpdateTime, memoryState, memoryUpdateTime)
}
