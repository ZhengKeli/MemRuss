package com.zkl.zklRussian.core.note

import com.zkl.zklRussian.core.note.base.*

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
) : Note
