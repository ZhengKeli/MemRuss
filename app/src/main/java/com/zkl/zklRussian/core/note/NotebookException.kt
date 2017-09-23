package com.zkl.zklRussian.core.note

open class NotebookException(
	message: String? = "The operation to notebook failed!"
) : RuntimeException(message)

class InternalNotebookException(
	message: String? = "An internal exception occurred!"
) : NotebookException(message)

class NoteIdNotFoundException(
	val noteId: Long,
	message: String? = "The note with id $noteId was not found!"
) : NotebookException(message)

class NoteTypeNotSupportedException(
	typeTag:String,
	message: String? = "The note with typeTag \"$typeTag\" is not supported! "
): NotebookException(message)

/**
 * 对[MutableNotebook]做操作的时候，
 * 出现了词条重复的冲突时，抛出此错误
 */
class DuplicateException(
	val uniqueTag: String,
	val duplicatedNoteId: Long,
	message: String = "The adding UniqueDraft with uniqueTag $uniqueTag duplicated with " +
		"another existed note with id $duplicatedNoteId"
) : NotebookException(message)