package com.zkl.zklRussian.control.note

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.zkl.zklRussian.control.tools.createIndex
import com.zkl.zklRussian.core.note.*
import org.jetbrains.anko.db.*


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
	
	
	val standardColumns= arrayOf(noteId, createTime,
		contentType, contentString, contentUpdateTime,
		memoryStatus, memoryProgress, memoryLoad, reviewTime, memoryUpdateTime)
	
	
}
private object UniqueTagTable {
	val tableName = "uniqueTags"
	
	val noteId = "noteId"
	val uniqueTag = "uniqueTag"
}
private object SearchTagTable {
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
	fun createTables(bookName:String) {
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
					confValue to 3,
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
				createTable(tableName,true,
					noteId to INTEGER + PRIMARY_KEY,
					uniqueTag to TEXT + UNIQUE)
				createIndex("${uniqueTag}Index", tableName, true, true, uniqueTag)
			}
			SearchTagTable.run {
				createTable(_name, true,
					noteId to INTEGER + PRIMARY_KEY,
					searchTag to TEXT)
			}
		}
	}
	
	
	//info
	override val version: Int = 3
	override var name: String
		get() = ConfsTable.run {
			database.select(tableName, confValue)
				.whereArgs("$confName = '$item_bookName' ")
				.exec {
					moveToFirst()
					getString(0)
				}
		}
		set(value) {
			ConfsTable.run {
				database.update(tableName, item_bookName to value)
					.whereArgs("$confName = '$item_bookName' ")
					.exec()
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
				database.delete(tableName,"$confName = '$item_memoryPlan' ")
			} else {
				val encoded = MemoryPlanCoder.encode(value)
				val nowTime = System.currentTimeMillis()
				
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
			}
		}
			
	override var memoryState: NotebookMemoryState
		get() = ConfsTable.run {
			database.select(tableName, confValue, confUpdateTime)
				.whereArgs("$confName = '$item_memoryState' ")
				.exec {
					//check data existence
					moveToFirst()
					if (!isAfterLast) NotebookMemoryCoder.decode(getString(0))
					else NotebookMemoryState.infantInstance
				}
		}
		private set(value) = ConfsTable.run {
			val nowTime = System.currentTimeMillis()
			val encoded = NotebookMemoryCoder.encode(value)
			database.update(tableName,
				confValue to encoded,
				confUpdateTime to nowTime)
				.whereArgs("$confName = '$item_memoryState' ")
				.exec()
			Unit
		}
	
	override val memorySummary: MemorySummary
		get() {
			val sumMemoryLoad = NotesTable.run {
				database.select(tableName, columns = "sum($memoryLoad)")
					.exec { this.getFloat(0) }
			}
			return MemorySummary(sumMemoryLoad)
		}
	
	
	//getters
	
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
			.getOrNull(0) ?: throw NoteIdNotFoundException(noteId)
	}
	
	override fun selectByKeyword(keyword: String, count: Int, offset: Int): List<Note> {
		return NotesTable.run {
			val sql = """
			SELECT DISTINCT ${standardColumns.joinToString(",")}
			FROM $tableName
			INNER JOIN ${SearchTagTable._name}
			ON $tableName.$noteId = ${SearchTagTable._name}.${SearchTagTable.noteId}
			WHERE ${SearchTagTable._name}.${SearchTagTable.searchTag}
			LIKE '%$keyword%'
			ORDER BY $tableName.$contentUpdateTime DESC """
			val cursor: Cursor = database.rawQuery(sql, null)
//			cursor.use { cursor.parseNoteList() }
			cursor.parseNoteList().also { cursor.close() }
		}
	}
	
	override fun selectLatestNotes(count: Int, offset: Int): List<Note> {
		return database.selectNotes()
			.orderBy(NotesTable.contentUpdateTime, SqlOrderDirection.DESC)
			.limit(offset, count)
			.exec { parseNoteList() }
	}
	
	override fun selectNeedReviewNotes(nowTime: Long, asc: Boolean, count: Int, offset: Int): List<Note> {
		return NotesTable.run {
			database.selectNotes()
				.whereArgs(" $reviewTime!=-1 AND $reviewTime<$nowTime ")
				.orderBy(reviewTime, if (asc) SqlOrderDirection.ASC else SqlOrderDirection.DESC)
				.limit(offset, count)
				.exec { parseNoteList() }
		}
	}
	
	override fun checkUniqueTag(tag: String,exceptId:Long): Long {
		return UniqueTagTable.run {
			database.select(tableName, noteId)
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
		return NotesTable.run {
			this@selectNotes.select(tableName, *standardColumns)
		}
	}
	private fun Cursor.parseNoteList():List<Note> = parseList(rowParser{
		noteId:Long,createTime:Long,
		contentType:String,contentString:String,contentUpdateTime:Long,
		memoryState:String,memoryProgress:Double,memoryLoad:Double,reviewTime:Long,memoryUpdateTime:Long ->
		
		val noteContentCoder = noteContentCoders[contentType] ?: throw NoteTypeNotSupportedException(contentType)
		val noteContent = noteContentCoder.decode(contentString)
		val noteMemory = NoteMemoryState(NoteMemoryStatus.valueOf(memoryState),memoryProgress,memoryLoad,reviewTime)
		
		Note(noteId,createTime,
			content = noteContent,
			contentUpdateTime = contentUpdateTime,
			memoryState = noteMemory,
			memoryUpdateTime = memoryUpdateTime)
	})
	
	
	
	//setters
	
	override fun withTransaction(action: () -> Unit) = database.transaction { action() }
	
	override fun addNote(content: NoteContent, memoryState: NoteMemoryState?): Long {
		
		val contentEncoder = noteContentCoders[content.typeTag] ?:
			throw NoteTypeNotSupportedException(content.typeTag)
		
		throwIfDuplicated(content.uniqueTags)
		
		val write_nowTime = System.currentTimeMillis()
		val write_noteContentString = contentEncoder.encode(content)
		val write_memory = memoryState ?: NoteMemoryState()
		
		var newNoteId: Long = -1L
		database.beginTransaction()
		database.endTransaction()
		database.transaction {
			NotesTable.run {
				newNoteId = insert(tableName,
					//noteId不用管
					createTime to write_nowTime,
					
					contentType to content.typeTag,
					contentString to write_noteContentString,
					contentUpdateTime to write_nowTime,
					
					this.memoryStatus to write_memory.status.name,
					memoryProgress to write_memory.progress,
					reviewTime to write_memory.reviewTime,
					memoryLoad to write_memory.load,
					memoryUpdateTime to write_nowTime
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
			delete(NotesTable.tableName, NotesTable.noteId + "=" + noteId, null)
			delete(UniqueTagTable.tableName, UniqueTagTable.noteId + "=" + noteId, null)
			delete(SearchTagTable._name, SearchTagTable.noteId + "=" + noteId, null)
		}
	}
	
	override fun modifyNoteContent(noteId: Long, content: NoteContent) {
		//检查改动后是否会产生冲突
		throwIfDuplicated(content.uniqueTags,noteId)
		
		database.transaction {
			//删除原有的 uniqueTags 和 searchTags
			UniqueTagTable.run { delete(tableName,"${UniqueTagTable.noteId} = $noteId") }
			SearchTagTable.run { delete(_name,"${SearchTagTable.noteId} = $noteId") }
			
			//添加note
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
					insert(tableName,
						UniqueTagTable.noteId to noteId,
						UniqueTagTable.uniqueTag to tag
					)
				}
			}
			SearchTagTable.run {
				content.searchTags.forEach { tag ->
					database.insert(_name,
						SearchTagTable.noteId to noteId,
						searchTag to tag
					)
				}
			}
		}
	}
	
	override fun modifyNoteMemory(noteId: Long, memoryState: NoteMemoryState) {
		val nowTime = System.currentTimeMillis()
		NotesTable.run {
			database.update(tableName,
				this.memoryStatus to memoryState.status.name,
				memoryProgress to memoryState.progress,
				memoryLoad to memoryState.load,
				reviewTime to memoryState.reviewTime,
				memoryUpdateTime to nowTime
			)
		}
	}
}


