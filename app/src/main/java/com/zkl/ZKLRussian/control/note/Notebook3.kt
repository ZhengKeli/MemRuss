package com.zkl.ZKLRussian.control.note

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.zkl.ZKLRussian.control.tools.createIndex
import com.zkl.ZKLRussian.core.note.*
import org.jetbrains.anko.db.*
import kotlin.reflect.jvm.internal.impl.utils.StringsKt


private object confTable {
	val _name = "conf"
	
	val item = "item"
	val value = "value"
	
	val item_version = "version"
	val item_bookName = "bookName"
	
}
private object notesTable {
	val _name = "notes"
	
	val noteId = "noteId"
	val createTime = "createTime"
	
	val contentType = "contentType"
	val contentString = "contentString"
	val contentUpdateTime = "contentUpdateTime"
	
	val memoryState = "memoryState"
	val memoryProgress = "memoryProgress"
	val memoryLoad = "memoryLoad"
	val reviewTime = "reviewTime"
	val memoryUpdateTime = "memoryUpdateTime"
	
	
	val standardColumns= arrayOf(
		noteId, createTime,
		contentType, contentString, contentUpdateTime,
		memoryState, memoryProgress, memoryLoad, reviewTime, memoryUpdateTime)
	
	
}
private object uniqueTagTable {
	val _name = "uniqueTags"
	
	val noteId = "noteId"
	val uniqueTag = "uniqueTag"
}
private object searchTagTable {
	val _name = "searchTags"
	
	val noteId = "noteId"
	val searchTag = "searchTag"
}


class MutableNotebook3
internal constructor(val database: SQLiteDatabase) : MutableNotebook {
	
	//life cycle
	override fun close() {
		database.close()
	}
	fun createTables(bookName:String="newBook") {
		database.transaction {
			confTable.run {
				createTable(_name, true,
					item to TEXT + PRIMARY_KEY,
					value to TEXT)
				insert(_name,
					item to item_version,
					value to 3)
				insert(_name,
					item to item_bookName,
					value to bookName)
			}
			notesTable.run {
				createTable(_name, true,
					noteId to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
					createTime to INTEGER,
					
					contentType to TEXT,
					contentString to TEXT,
					contentUpdateTime to INTEGER,
					
					memoryState to INTEGER,
					memoryProgress to REAL,
					memoryLoad to REAL,
					reviewTime to INTEGER,
					memoryUpdateTime to INTEGER
				)
				createIndex("${createTime}Index",_name,true, createTime)
				createIndex("${contentUpdateTime}Index", _name,true,contentUpdateTime)
				createIndex("${reviewTime}Index", _name,true,reviewTime)
			}
			uniqueTagTable.run {
				createTable(_name,true,
					noteId to INTEGER + PRIMARY_KEY,
					uniqueTag to TEXT + UNIQUE)
			}
			searchTagTable.run {
				createTable(_name, true,
					noteId to INTEGER + PRIMARY_KEY,
					searchTag to TEXT)
			}
		}
	}
	
	
	//info
	override val name: String by lazy {
		confTable.run {
			database.select(_name, value)
				.whereArgs("$item = '$item_bookName' ")
				.exec {
					moveToFirst()
					getString(0)
				}
		}
	}
	override val workLoad: Float
		get() = notesTable.run {
			database.select(_name,"sum($workLoad)")
				.exec { getFloat(0) }
		}
	
	
	//getters
	
	override val noteCount: Int get() {
		return database.select(notesTable._name, "count(*)").exec {
			moveToFirst()
			getInt(0)
		}
	}
	
	override fun getNote(noteId: Long): Note {
		return database.selectNotes()
			.whereArgs(notesTable.noteId + "=" + noteId)
			.exec { decodeNotes() }
			.getOrNull(0) ?: throw NoteIdNotFoundException(noteId)
	}
	
	override fun selectLatestNotes(count: Int, offset: Int): List<Note> {
		return database.selectNotes()
			.orderBy(notesTable.contentUpdateTime,SqlOrderDirection.DESC)
			.limit(offset, count)
			.exec { decodeNotes() }
	}
	
	override fun selectByKeyword(keyword: String, count: Int, offset: Int): List<Note> {
		return notesTable.run {
			val sql = """
			SELECT DISTINCT ${StringsKt.join(standardColumns.asIterable(), ",")}
			FROM $_name
			INNER JOIN ${searchTagTable._name}
			ON $_name.$noteId = ${searchTagTable._name}.${searchTagTable.noteId}
			WHERE ${searchTagTable._name}.${searchTagTable.searchTag}
			LIKE '%$keyword%'
			ORDER BY $_name.$contentUpdateTime DESC """
			
			database.rawQuery(sql, null).use { cursor -> cursor.decodeNotes() }
		}
	}
	
	override fun selectNeedReviewNotes(nowTime: Long, asc: Boolean, count: Int, offset: Int): List<Note> {
		return notesTable.run {
			database.selectNotes()
				.whereArgs(" $reviewTime!=-1 AND $reviewTime<$nowTime ")
				.orderBy(reviewTime, if (asc) SqlOrderDirection.ASC else SqlOrderDirection.DESC)
				.limit(offset, count)
				.exec { decodeNotes() }
		}
	}
	
	override fun checkUniqueTag(tag: String,exceptId:Long): Long {
		return uniqueTagTable.run {
			database.select(_name, noteId)
				.whereArgs("$uniqueTag = '$tag' and $noteId != $exceptId")
				.limit(1)
				.exec {
					moveToFirst()
					if (isAfterLast) -1
					else getLong(0)
				}
		}
	}
	
	
	//private getters
	private fun SQLiteDatabase.selectNotes(): SelectQueryBuilder {
		return notesTable.run {
			this@selectNotes.select(_name, *standardColumns)
		}
	}
	private fun Cursor.decodeNotes():List<Note>{
		val read_notes = ArrayList<Note>(count)
		moveToFirst()
		
		val index_noteId = getColumnIndex(notesTable.noteId)
		val index_createTime = getColumnIndex(notesTable.createTime)
		
		val index_typeTag = getColumnIndex(notesTable.contentType)
		val index_noteContent = getColumnIndex(notesTable.contentString)
		val index_contentUpdateTime = getColumnIndex(notesTable.contentUpdateTime)
		
		val index_memoryState = getColumnIndex(notesTable.memoryState)
		val index_memoryProgress = getColumnIndex(notesTable.memoryProgress)
		val index_memoryLoad = getColumnIndex(notesTable.memoryLoad)
		val index_reviewTime = getColumnIndex(notesTable.reviewTime)
		val index_memoryUpdateTime = getColumnIndex(notesTable.memoryUpdateTime)
		
		while (!isAfterLast) {
			val read_noteId = getLong(index_noteId)
			val read_createTime = getLong(index_createTime)
			
			val read_typeTag = getString(index_typeTag)
			val read_noteContentString = getString(index_noteContent)
			val read_contentUpdateTime = getLong(index_contentUpdateTime)
			
			val read_memoryState = MemoryState.values()[getInt(index_memoryState)]
			val read_memoryProgress = getFloat(index_memoryProgress)
			val read_memoryLoad = getFloat(index_memoryLoad)
			val read_reviewTime = getLong(index_reviewTime)
			val read_memoryUpdateTime = getLong(index_memoryUpdateTime)
			
			val noteContentCoder = noteContentCoders[read_typeTag] ?: throw NoteTypeNotSupportedException(read_typeTag)
			val read_noteContent = noteContentCoder.decode(read_noteContentString)
			val read_noteMemory = NoteMemory(read_memoryState,read_memoryProgress,read_memoryLoad,read_reviewTime)
			
			val read_note = Note(
				id = read_noteId,
				createTime = read_createTime,
				
				content = read_noteContent,
				contentUpdateTime = read_contentUpdateTime,
				
				memory = read_noteMemory,
				memoryUpdateTime = read_memoryUpdateTime
			)
			
			read_notes.add(read_note)
			
			moveToNext()
		}
		return read_notes
	}
	
	
	//setters
	override fun withTransaction(action: () -> Unit) = database.transaction { action() }
	
	override fun addNote(content: NoteContent, memory: NoteMemory?): Long {
		
		val contentEncoder = noteContentCoders[content.typeTag] ?:
			throw NoteTypeNotSupportedException(content.typeTag)
		
		throwIfDuplicated(content.uniqueTags)
		
		val write_nowTime = System.currentTimeMillis()
		val write_noteContentString = contentEncoder.encode(content)
		val write_memory = memory ?: NoteMemory()
		
		var newNoteId: Long = -1L
		database.beginTransaction()
		database.endTransaction()
		database.transaction {
			notesTable.run {
				newNoteId = insert(_name,
					//noteId不用管
					createTime to write_nowTime,
					
					contentType to content.typeTag,
					contentString to write_noteContentString,
					contentUpdateTime to write_nowTime,
					
					memoryState to write_memory.state.ordinal,
					memoryProgress to write_memory.progress,
					reviewTime to write_memory.reviewTime,
					memoryLoad to write_memory.load,
					memoryUpdateTime to write_nowTime
				)
				if (newNoteId == -1L) throw InternalNotebookException("failed to insert new note")
			}
			uniqueTagTable.run {
				content.uniqueTags.forEach { tag ->
					insert(_name,
						uniqueTagTable.noteId to newNoteId,
						uniqueTagTable.uniqueTag to tag
					)
				}
			}
			searchTagTable.run {
				content.searchTags.forEach { tag ->
					insert(_name,
						noteId to newNoteId,
						searchTag to tag
					)
				}
			}
		}
		return newNoteId
	}
	
	override fun deleteNote(noteId: Long) {
		return database.transaction {
			delete(notesTable._name, notesTable.noteId + "=" + noteId, null)
			delete(uniqueTagTable._name, uniqueTagTable.noteId + "=" + noteId, null)
			delete(searchTagTable._name, searchTagTable.noteId + "=" + noteId, null)
		}
	}
	
	override fun modifyNoteContent(noteId: Long, noteContent: NoteContent) {
		//检查改动后是否会产生冲突
		throwIfDuplicated(noteContent.uniqueTags,noteId)
		
		database.transaction {
			//删除原有的 uniqueTags 和 searchTags
			uniqueTagTable.run { delete(_name,"${uniqueTagTable.noteId} = $noteId") }
			searchTagTable.run { delete(_name,"${searchTagTable.noteId} = $noteId") }
			
			//添加note
			notesTable.run {
				val newContentCoder = noteContentCoders[noteContent.typeTag] ?:
					throw NoteTypeNotSupportedException(noteContent.typeTag)
				val nowTime = System.currentTimeMillis()
				
				update(_name,
					contentType to noteContent.typeTag,
					contentString to newContentCoder.encode(noteContent),
					contentUpdateTime to nowTime)
					.whereArgs("${notesTable.noteId} = $noteId")
					.exec()
			}
			
			//添加新的 uniqueTags 和 searchTags
			uniqueTagTable.run {
				noteContent.uniqueTags.forEach { tag ->
					insert(_name,
						uniqueTagTable.noteId to noteId,
						uniqueTagTable.uniqueTag to tag
					)
				}
			}
			searchTagTable.run {
				noteContent.searchTags.forEach { tag ->
					database.insert(_name,
						searchTagTable.noteId to noteId,
						searchTag to tag
					)
				}
			}
		}
	}
	
	override fun modifyNoteMemory(noteId: Long, noteMemory: NoteMemory) {
		val nowTime = System.currentTimeMillis()
		notesTable.run {
			database.update(_name,
				memoryState to noteMemory.state.ordinal,
				memoryProgress to noteMemory.progress,
				memoryLoad to noteMemory.load,
				reviewTime to noteMemory.reviewTime,
				memoryUpdateTime to nowTime
			)
		}
	}
}


