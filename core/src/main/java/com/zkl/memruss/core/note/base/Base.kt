package com.zkl.memruss.core.note.base

import com.zkl.memruss.core.note.NoteContent
import java.io.Closeable

interface BaseNotebook<Note : BaseNote<*>> : Closeable {
	
	//info
	
	/**
	 * 该单词本版本
	 */
	val version: Int
	
	/**
	 * 该单词本的名字
	 */
	val name: String
	
	
	//notes
	
	/**
	 * 词条总数量
	 */
	val noteCount: Int
	
	/**
	 * 根据 noteId 获取一个词条
	 * @return 对应 noteId 的词条
	 * @throws NoteIdNotFoundException 当对应 noteId 的词条没找到时抛出错误
	 */
	@Throws(NoteIdNotFoundException::class)
	fun getNote(noteId: Long): Note
	
	/**
	 * 不要求任何顺序地获取词条
	 */
	fun rawGetNotes(count: Int = 128, offset: Int = 0): List<Note>
	
	/**
	 * 获取所有词条
	 */
	fun rawGetAllNotes(): Collection<Note> = object : AbstractCollection<Note>() {
		override val size: Int = noteCount
		override fun iterator(): Iterator<Note> = object : AbstractIterator<Note>() {
			var index = 0
			val sectionSize = 1024
			var section = emptyList<Note>()
			override fun computeNext() {
				if (index < size) {
					val indexInBuffer = index % sectionSize
					if (indexInBuffer == 0) {
						val count = Math.min(sectionSize, size - index)
						section = rawGetNotes(count, index)
					}
					setNext(section[indexInBuffer])
					index++
				} else done()
			}
		}
	}
	
	/**
	 * 获得最新的一些词条
	 * @param count 获取的最大词条数
	 * @param offset 可以跳过最开始的几个词条，返回后面的几个
	 * @return 最新的一些词条，按照修改日期倒序排序
	 */
	fun selectLatestNotes(count: Int = 100, offset: Int = 0): List<Note>
	
}

interface MutableBaseNotebook<Content : BaseContent, Note : BaseNote<Content>> : BaseNotebook<Note> {
	
	//info
	override var name: String
	
	
	//notes
	
	/**
	 * 添加一个词条
	 * @return 返回刚加入的词条的 noteId
	 */
	fun addNote(content: Content): Long
	
	/**
	 * 完全保留地添加一个词条
	 */
	fun rawAddNote(note: Note): Long
	
	/**
	 * 修改 note 的内容
	 */
	fun modifyNoteContent(noteId: Long, content: Content)
	
	/**
	 * 根据 noteId 删除一个词条
	 */
	fun deleteNote(noteId: Long)
	
}

interface BaseNote<out NoteContent : BaseContent> {
	
	//basic info
	
	/**
	 * 在同一本单词本中区别于其他词条的 id
	 */
	val id: Long
	
	/**
	 * 词条被创建的时间的毫秒时间戳
	 */
	val createTime: Long
	
	
	//content
	
	/**
	 * 词条的内容，其类型可能会不同
	 */
	val content: NoteContent
	
	/**
	 * 词条内容最后一次被修改的毫秒时间戳
	 */
	val contentUpdateTime: Long
	
}

interface BaseContent {
	/**
	 * 不同种类的[NoteContent]的类型标签
	 */
	val typeTag: String get() = this.javaClass.simpleName
}


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
	typeTag: String,
	message: String? = "The note with typeTag \"$typeTag\" is not supported! "
) : NotebookException(message)


fun <Note : BaseNote<*>> BaseNotebook<Note>.getNoteOrNull(noteId: Long): Note? {
	return try {
		getNote(noteId)
	} catch (e: NoteIdNotFoundException) {
		null
	}
}
