package com.zkl.memruss.control.note

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.zkl.memruss.control.tools.createIndex
import com.zkl.memruss.control.tools.orderBy
import com.zkl.memruss.core.note.InstantNote
import com.zkl.memruss.core.note.MutableNotebook
import com.zkl.memruss.core.note.Note
import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.base.*
import org.jetbrains.anko.db.*
import org.jetbrains.anko.db.ConflictClause.ABORT


private object ConfsTable {
	val tableName = "confs"
	
	val confName = "confName"
	val confValue = "confValue"
	val confCreateTime = "confCreateTime"
	val confUpdateTime = "confUpdateTime"
	
	val item_version = "version"
	val item_bookName = "bookName"
	val item_memoryState = "memoryState"
	val item_memoryPlan = "memoryPlan"
	
}

private object NotesTable {
	val tableName = "notes"
	
	val noteId = "noteId"
	val createTime = "createTime"
	
	val contentType = "contentType"
	val contentString = "contentString"
	val contentUpdateTime = "contentUpdateTime"
	
	val memoryStatus = "memoryStatus"
	val memoryProgress = "memoryProgress"
	val memoryLoad = "memoryLoad"
	val reviewTime = "reviewTime"
	val memoryUpdateTime = "memoryUpdateTime"
	
	
	val standardColumns = arrayOf(noteId, createTime,
		contentType, contentString, contentUpdateTime,
		memoryStatus, memoryProgress, memoryLoad, reviewTime, memoryUpdateTime)
	val standardColumnsWithTableName = standardColumns.map { tableName + "." + it }
	
}

private object UniqueTagTable {
	val tableName = "uniqueTags"
	
	val noteId = "noteId"
	val uniqueTag = "uniqueTag"
}

private object SearchTagTable {
	val tableName = "searchTags"
	
	val noteId = "noteId"
	val searchTag = "searchTag"
}

class MutableNotebook3(val database: SQLiteDatabase) : MutableNotebook {
	
	companion object {
		val VERSION = 3
	}
	
	//life cycle
	fun createTables(bookName: String) {
		database.transaction {
			val nowTime = System.currentTimeMillis()
			ConfsTable.run {
				createTable(tableName, true,
					confName to TEXT + PRIMARY_KEY,
					confValue to TEXT,
					confCreateTime to INTEGER,
					confUpdateTime to INTEGER)
				insert(tableName,
					confName to item_version,
					confValue to VERSION,
					confCreateTime to nowTime,
					confUpdateTime to nowTime)
				insert(tableName,
					confName to item_bookName,
					confValue to bookName,
					confCreateTime to nowTime,
					confUpdateTime to nowTime)
			}
			NotesTable.run {
				createTable(tableName, true,
					noteId to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
					createTime to INTEGER,
					
					contentType to TEXT,
					contentString to TEXT,
					contentUpdateTime to INTEGER,
					
					memoryStatus to TEXT,
					memoryProgress to REAL,
					memoryLoad to REAL,
					reviewTime to INTEGER,
					memoryUpdateTime to INTEGER
				)
				createIndex("${createTime}Index", tableName, false, true, createTime)
				createIndex("${contentUpdateTime}Index", tableName, false, true, contentUpdateTime)
				createIndex("${reviewTime}Index", tableName, false, true, reviewTime)
				Unit
			}
			UniqueTagTable.run {
				createTable(tableName, true,
					noteId to INTEGER,
					uniqueTag to TEXT + PRIMARY_KEY + UNIQUE(ABORT))
				createIndex("${uniqueTag}Index", tableName, true, true, uniqueTag)
			}
			SearchTagTable.run {
				createTable(tableName, true,
					noteId to INTEGER,
					searchTag to TEXT)
				createIndex("${searchTag}Index", tableName, false, true, searchTag)
			}
		}
	}
	
	fun checkVersion(): Boolean {
		return try {
			val version = ConfsTable.run {
				database.select(tableName, confValue)
					.whereArgs("$confName = '$item_version' ")
					.exec {
						moveToFirst()
						getInt(0)
					}
			}
			version == VERSION
		}catch (e:Exception){
			false
		}
	}
	
	override fun close() {
		database.close()
	}
	
	
	
	//info
	override val version: Int = VERSION
	
	override var name: String
		get() = ConfsTable.run {
			database.select(tableName, confValue)
				.whereArgs("$confName = '$item_bookName' ")
				.exec {
					moveToFirst()
					getString(0)
				}
		}
		set(value) = ConfsTable.run {
			database.update(tableName, confValue to value)
				.whereArgs("$confName = '$item_bookName' ")
				.exec()
			
		}
	
	
	
	//note getters
	override val noteCount: Int get() {
			return database.select(NotesTable.tableName, "count(*)").exec {
				moveToFirst()
				getInt(0)
			}
		}
	
	override fun getNote(noteId: Long): Note {
		return database.selectNotes()
			.whereArgs(NotesTable.noteId + "=" + noteId)
			.exec { parseNoteList() }
			.firstOrNull() ?: throw NoteIdNotFoundException(noteId)
	}
	
	override fun rawGetNotes(count: Int, offset: Int): List<Note> {
		return database.selectNotes()
			.limit(offset, count)
			.exec { parseNoteList() }
	}
	
	override fun selectLatestNotes(count: Int, offset: Int): List<Note> {
		return database.selectNotes()
			.orderBy(NotesTable.contentUpdateTime, SqlOrderDirection.DESC)
			.limit(offset, count)
			.exec { parseNoteList() }
	}
	
	override fun selectByKeyword(keyword: String, count: Int, offset: Int): List<Note> {
		val sql = NotesTable.run {
			"""
			SELECT DISTINCT ${standardColumnsWithTableName.joinToString(",")}
			FROM $tableName
			INNER JOIN ${SearchTagTable.tableName}
			ON $tableName.$noteId = ${SearchTagTable.tableName}.${SearchTagTable.noteId}
			WHERE ${SearchTagTable.tableName}.${SearchTagTable.searchTag} LIKE '%$keyword%'
			ORDER BY $tableName.$contentUpdateTime DESC
			"""
		}
		return database.rawQuery(sql, null).use {
			it.parseNoteList()
		}
	}
	
	override fun checkUniqueTag(uniqueTag: String, exceptId: Long): Long {
		return UniqueTagTable.run {
			database.select(tableName, noteId)
				.whereArgs("${this.uniqueTag} = '$uniqueTag' and $noteId != $exceptId")
				.limit(1)
				.exec {
					moveToFirst()
					if (isAfterLast) -1
					else getLong(0)
				}
		}
	}
	
	//note setters
	override fun addNote(content: NoteContent): Long {
		throwIfDuplicated(content.uniqueTags)
		
		val contentEncoder = noteContentCoders[content.typeTag] ?:
			throw NoteTypeNotSupportedException(content.typeTag)
		
		val nowTime = System.currentTimeMillis()
		val noteContentString = contentEncoder.encode(content)
		val noteMemoryState = NoteMemoryState.infantState()
		
		var newNoteId: Long = -1L
		database.transaction {
			NotesTable.run {
				newNoteId = insert(tableName,
					//noteId不用管
					createTime to nowTime,
					
					contentType to content.typeTag,
					contentString to noteContentString,
					contentUpdateTime to nowTime,
					
					memoryStatus to noteMemoryState.status.name,
					memoryProgress to noteMemoryState.progress,
					reviewTime to noteMemoryState.reviewTime,
					memoryLoad to noteMemoryState.load,
					memoryUpdateTime to nowTime
				)
				if (newNoteId == -1L) throw InternalNotebookException("failed to insert new note")
			}
			UniqueTagTable.run {
				content.uniqueTags.forEach { tag ->
					insert(tableName,
						UniqueTagTable.noteId to newNoteId,
						UniqueTagTable.uniqueTag to tag
					)
				}
			}
			SearchTagTable.run {
				content.searchTags.forEach { tag ->
					insert(tableName,
						noteId to newNoteId,
						searchTag to tag
					)
				}
			}
		}
		return newNoteId
	}
	
	override fun rawAddNote(note: Note): Long {
		val content = note.content
		throwIfDuplicated(content.uniqueTags)
		
		val contentEncoder = noteContentCoders[content.typeTag] ?:
			throw NoteTypeNotSupportedException(content.typeTag)
		val noteContentString = contentEncoder.encode(content)
		
		var newNoteId: Long = -1L
		database.transaction {
			NotesTable.run {
				newNoteId = insert(tableName,
					//noteId不用管
					createTime to note.createTime,
					
					contentType to content.typeTag,
					contentString to noteContentString,
					contentUpdateTime to note.contentUpdateTime,
					
					memoryStatus to note.memoryState.status.name,
					memoryProgress to note.memoryState.progress,
					reviewTime to note.memoryState.reviewTime,
					memoryLoad to note.memoryState.load,
					memoryUpdateTime to note.memoryUpdateTime
				)
				if (newNoteId == -1L) throw InternalNotebookException("failed to insert new note")
			}
			UniqueTagTable.run {
				content.uniqueTags.forEach { tag ->
					insert(tableName,
						UniqueTagTable.noteId to newNoteId,
						UniqueTagTable.uniqueTag to tag
					)
				}
			}
			SearchTagTable.run {
				content.searchTags.forEach { tag ->
					insert(tableName,
						noteId to newNoteId,
						searchTag to tag
					)
				}
			}
		}
		return newNoteId
	}
	
	override fun modifyNoteContent(noteId: Long, content: NoteContent) {
		//检查改动后是否会产生冲突
		throwIfDuplicated(content.uniqueTags, noteId)
		
		database.transaction {
			//删除原有的 uniqueTags 和 searchTags
			UniqueTagTable.run { delete(tableName, "${UniqueTagTable.noteId} = $noteId") }
			SearchTagTable.run { delete(tableName, "${SearchTagTable.noteId} = $noteId") }
			
			//修改note
			NotesTable.run {
				val newContentCoder = noteContentCoders[content.typeTag] ?:
					throw NoteTypeNotSupportedException(content.typeTag)
				val nowTime = System.currentTimeMillis()
				
				update(tableName,
					contentType to content.typeTag,
					contentString to newContentCoder.encode(content),
					contentUpdateTime to nowTime)
					.whereArgs("${NotesTable.noteId} = $noteId")
					.exec()
			}
			
			//添加新的 uniqueTags 和 searchTags
			UniqueTagTable.run {
				content.uniqueTags.forEach { tag ->
					insert(UniqueTagTable.tableName,
						UniqueTagTable.noteId to noteId,
						UniqueTagTable.uniqueTag to tag
					)
				}
			}
			SearchTagTable.run {
				content.searchTags.forEach { tag ->
					database.insert(SearchTagTable.tableName,
						SearchTagTable.noteId to noteId,
						SearchTagTable.searchTag to tag
					)
				}
			}
		}
	}
	
	override fun deleteNote(noteId: Long) {
		return database.transaction {
			delete(NotesTable.tableName, NotesTable.noteId + "=" + noteId, null)
			delete(UniqueTagTable.tableName, UniqueTagTable.noteId + "=" + noteId, null)
			delete(SearchTagTable.tableName, SearchTagTable.noteId + "=" + noteId, null)
		}
	}
	
	//private methods for getters and setters
	private fun SQLiteDatabase.selectNotes(): SelectQueryBuilder {
		return NotesTable.run {
			this@selectNotes.select(tableName, *standardColumns)
		}
	}
	
	private fun Cursor.parseNoteList(): List<Note> = parseList(rowParser {
		noteId: Long, createTime: Long,
		contentType: String, contentString: String, contentUpdateTime: Long,
		memoryState: String, memoryProgress: Double, memoryLoad: Double, reviewTime: Long, memoryUpdateTime: Long ->
		
		val noteContentCoder = noteContentCoders[contentType] ?: throw NoteTypeNotSupportedException(contentType)
		val noteContent = noteContentCoder.decode(contentString)
		val noteMemory = NoteMemoryState(NoteMemoryStatus.valueOf(memoryState), memoryProgress, memoryLoad, reviewTime)
		
		InstantNote(noteId, createTime,
			content = noteContent,
			contentUpdateTime = contentUpdateTime,
			memoryState = noteMemory,
			memoryUpdateTime = memoryUpdateTime)
	})
	
	@Throws(NoteConflictException::class)
	private fun throwIfDuplicated(uniqueTags: Collection<String>, exceptId: Long = -1L) {
		uniqueTags.forEach { uniqueTag ->
			val id = checkUniqueTag(uniqueTag, exceptId)
			if (id != -1L) throw NoteConflictException(uniqueTag, id)
		}
	}
	
	
	
	//memory
	override var memoryPlan: MemoryPlan?
		get() = ConfsTable.run {
			database.select(tableName, confValue)
				.whereArgs("$confName = '$item_memoryPlan' ")
				.exec {
					moveToFirst()
					return@exec if (isAfterLast) null
					else MemoryPlanCoder.decode(getString(0))
				}
		}
		set(value) = ConfsTable.run {
			if (value == null) {
				database.delete(tableName, "$confName = '$item_memoryPlan' ")
				memoryState = NotebookMemoryState.infantState
			}
			else {
				val nowTime = System.currentTimeMillis()
				val encoded = MemoryPlanCoder.encode(value)
				val updated = database.update(tableName,
					confValue to encoded,
					confUpdateTime to nowTime)
					.whereArgs("$confName = '$item_memoryPlan' ")
					.exec() == 1
				if (!updated) database.insert(tableName,
					confName to item_memoryPlan,
					confValue to encoded,
					confCreateTime to nowTime,
					confUpdateTime to nowTime)
				
				//是否要将state设为开启
				if (memoryState.status == NotebookMemoryStatus.INFANT)
					memoryState = NotebookMemoryState.beginningState(nowTime)
				
			}
		}
	
	override var memoryState: NotebookMemoryState
		get() = ConfsTable.run {
			database.select(tableName, confValue, confUpdateTime)
				.whereArgs("$confName = '$item_memoryState' ")
				.exec {
					moveToFirst()
					if (!isAfterLast) NotebookMemoryStateCoder.decode(getString(0))
					else NotebookMemoryState.infantState
				}
		}
		set(value) = ConfsTable.run {
			val nowTime = System.currentTimeMillis()
			
			val encoded = NotebookMemoryStateCoder.encode(value)
			val updated = database.update(tableName,
				confValue to encoded,
				confUpdateTime to nowTime)
				.whereArgs("$confName = '$item_memoryState' ")
				.exec() == 1
			if (!updated) database.insert(tableName,
				confName to item_memoryState,
				confValue to encoded,
				confCreateTime to nowTime,
				confUpdateTime to nowTime)
			
			if (value.status == NotebookMemoryStatus.INFANT) {
				//reset all MemoryState of all Notes
				NotesTable.run {
					val infantState = NoteMemoryState.infantState()
					database.update(tableName,
						memoryStatus to infantState.status.name,
						memoryProgress to infantState.progress,
						memoryLoad to infantState.load,
						reviewTime to infantState.reviewTime,
						memoryUpdateTime to nowTime)
						.whereArgs("$memoryStatus != '${infantState.status}'")
						.exec()
				}
			}
			
			Unit
		}
	
	override fun modifyNoteMemory(noteId: Long, memoryState: NoteMemoryState) {
		val nowTime = System.currentTimeMillis()
		NotesTable.run {
			database.update(tableName,
				memoryStatus to memoryState.status.name,
				memoryProgress to memoryState.progress,
				memoryLoad to memoryState.load,
				reviewTime to memoryState.reviewTime,
				memoryUpdateTime to nowTime)
				.whereArgs("${NotesTable.noteId}=$noteId")
				.exec()
		}
	}
	
	override fun selectNeedActivateNoteIds(asc: Boolean, count: Int, offset: Int): List<Long> {
		return database.select(NotesTable.tableName, NotesTable.noteId)
			.whereArgs("${NotesTable.memoryStatus}='${NoteMemoryStatus.INFANT}'")
			.orderBy(NotesTable.contentUpdateTime, asc)
			.limit(offset, count)
			.exec {
				parseList(rowParser{noteId:Long-> noteId })
			}
	}
	
	override fun countNeedReviewNotes(nowTime: Long): Int {
		return NotesTable.run {
			database.select(tableName, "count(*)")
				.whereArgs(" $reviewTime!=-1 AND $reviewTime<$nowTime ")
				.exec {
					moveToFirst()
					getInt(0)
				}
		}
	}
	
	override fun selectNeedReviewNotes(nowTime: Long, asc: Boolean, count: Int, offset: Int): List<Note> {
		return NotesTable.run {
			database.selectNotes()
				.whereArgs(" $reviewTime!=-1 AND $reviewTime<$nowTime ")
				.orderBy(reviewTime, asc)
				.limit(offset, count)
				.exec { parseNoteList() }
		}
	}
	
	override fun sumMemoryLoad(): Double = NotesTable.run {
		database.select(tableName, "sum($memoryLoad)")
			.exec {
				moveToFirst()
				getDouble(0)
			}
	}
	
}
