package com.zkl.ZKLRussian.control.note

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.zkl.ZKLRussian.control.tools.createIndex
import com.zkl.ZKLRussian.core.note.*
import org.jetbrains.anko.db.*


private object confsTable {
	val tableName = "confs"
	
	val confName = "confName"
	val confValue = "confValue"
	val confCreateTime = "confCreateTime"
	val confUpdateTime = "confUpdateTime"
	
	val item_version = "version"
	val item_bookName = "bookName"
	val item_memory = "memory"
	val item_memoryPlan = "memoryPlan"
	
}
private object notesTable {
	val tableName = "notes"
	
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
	
	
	val standardColumns= arrayOf(noteId, createTime,
		contentType, contentString, contentUpdateTime,
		memoryState, memoryProgress, memoryLoad, reviewTime, memoryUpdateTime)
	
	
}
private object uniqueTagTable {
	val tableName = "uniqueTags"
	
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
	fun createTables(bookName:String) {
		database.transaction {
			val nowTime = System.currentTimeMillis()
			confsTable.run {
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
			notesTable.run {
				createTable(tableName, true,
					noteId to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
					createTime to INTEGER,
					
					contentType to TEXT,
					contentString to TEXT,
					contentUpdateTime to INTEGER,
					
					memoryState to TEXT,
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
			uniqueTagTable.run {
				createTable(tableName,true,
					noteId to INTEGER + PRIMARY_KEY,
					uniqueTag to TEXT + UNIQUE)
				createIndex("${uniqueTag}Index", tableName, true, true, uniqueTag)
			}
			searchTagTable.run {
				createTable(_name, true,
					noteId to INTEGER + PRIMARY_KEY,
					searchTag to TEXT)
			}
		}
	}
	
	
	//info
	override val version: Int = 3
	override var name: String
		get() = confsTable.run {
			database.select(tableName, confValue)
				.whereArgs("$confName = '$item_bookName' ")
				.exec {
					moveToFirst()
					getString(0)
				}
		}
		set(value) {
			confsTable.run {
				database.update(tableName, item_bookName to value)
					.whereArgs("$confName = '$item_bookName' ")
					.exec()
			}
		}
	
	
	
	//memory
	override var memoryPlan: MemoryPlan?
		get() = confsTable.run {
			database.select(tableName, confValue)
				.whereArgs("$confName = '$item_memoryPlan' ")
				.exec {
					moveToFirst()
					return@exec if (isAfterLast) null
					else MemoryPlanCoder.decode(getString(0))
				}
		}
		set(value) = confsTable.run {
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
			
	override var memory: NotebookMemory
		get() = confsTable.run {
			database.select(tableName, confValue, confUpdateTime)
				.whereArgs("$confName = '$item_memory' ")
				.exec {
					//check data existence
					moveToFirst()
					if (isAfterLast) return@exec NotebookMemory.infantInstance
					
					//read data
					val oldMemory = NotebookMemoryCoder.decode(getString(0))
					val modifyTime = getLong(1)
					
					//check isInvalid
					val nowTime = System.currentTimeMillis()
					val isInvalid = Math.abs(nowTime - modifyTime) > 1000 * 3600 * 24
					
					return@exec when (isInvalid) {
						true -> recomputeMemoryLoad(oldMemory, true)
						false -> oldMemory
					}
				}
		}
		set(value) = confsTable.run {
				val nowTime = System.currentTimeMillis()
				val encoded = NotebookMemoryCoder.encode(value)
				database.update(tableName,
					confValue to encoded,
					confUpdateTime to nowTime)
					.whereArgs("$confName = '$item_memory' ")
					.exec()
				Unit
			}
	
	override val memorySummary: MemorySummary
		get() {
			val sumMemoryLoad = notesTable.run {
				database.select(tableName, columns = "sum($memoryLoad)")
					.exec { this.getFloat(0) }
			}
			return MemorySummary(sumMemoryLoad)
		}
	
	//private memory
	private fun recomputeMemoryLoad(oldMemory: NotebookMemory,writeIn:Boolean = true): NotebookMemory {
		val recomputedSumLoad = notesTable.run {
			database.select(tableName, columns = "sum($memoryLoad)")
				.exec { this.getDouble(0) }
		}
		val newMemory = oldMemory.copy(sumLoad = recomputedSumLoad)
		if(writeIn) this.memory = newMemory
		return newMemory
	}
	
	
	
	//getters
	
	override val noteCount: Int get() {
		return database.select(notesTable.tableName, "count(*)").exec {
			moveToFirst()
			getInt(0)
		}
	}
	
	override fun getNote(noteId: Long): Note {
		return database.selectNotes()
			.whereArgs(notesTable.noteId + "=" + noteId)
			.exec { parseNoteList() }
			.getOrNull(0) ?: throw NoteIdNotFoundException(noteId)
	}
	
	override fun selectByKeyword(keyword: String, count: Int, offset: Int): List<Note> {
		return notesTable.run {
			val sql = """
			SELECT DISTINCT ${standardColumns.joinToString(",")}
			FROM $tableName
			INNER JOIN ${searchTagTable._name}
			ON $tableName.$noteId = ${searchTagTable._name}.${searchTagTable.noteId}
			WHERE ${searchTagTable._name}.${searchTagTable.searchTag}
			LIKE '%$keyword%'
			ORDER BY $tableName.$contentUpdateTime DESC """
			database.rawQuery(sql, null).use { cursor -> cursor.parseNoteList() }
		}
	}
	
	override fun selectLatestNotes(count: Int, offset: Int): List<Note> {
		return database.selectNotes()
			.orderBy(notesTable.contentUpdateTime, SqlOrderDirection.DESC)
			.limit(offset, count)
			.exec { parseNoteList() }
	}
	
	override fun selectNeedReviewNotes(nowTime: Long, asc: Boolean, count: Int, offset: Int): List<Note> {
		return notesTable.run {
			database.selectNotes()
				.whereArgs(" $reviewTime!=-1 AND $reviewTime<$nowTime ")
				.orderBy(reviewTime, if (asc) SqlOrderDirection.ASC else SqlOrderDirection.DESC)
				.limit(offset, count)
				.exec { parseNoteList() }
		}
	}
	
	override fun checkUniqueTag(tag: String,exceptId:Long): Long {
		return uniqueTagTable.run {
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
		return notesTable.run {
			this@selectNotes.select(tableName, *standardColumns)
		}
	}
	private fun Cursor.parseNoteList():List<Note> = parseList(rowParser{
		noteId:Long,createTime:Long,
		contentType:String,contentString:String,contentUpdateTime:Long,
		memoryState:String,memoryProgress:Double,memoryLoad:Double,reviewTime:Long,memoryUpdateTime:Long ->
		
		val noteContentCoder = noteContentCoders[contentType] ?: throw NoteTypeNotSupportedException(contentType)
		val noteContent = noteContentCoder.decode(contentString)
		val noteMemory = NoteMemory(NoteMemoryState.valueOf(memoryState),memoryProgress,memoryLoad,reviewTime)
		
		Note(noteId,createTime,
			content = noteContent,
			contentUpdateTime = contentUpdateTime,
			memory = noteMemory,
			memoryUpdateTime = memoryUpdateTime)
	})
	
	
	
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
				newNoteId = insert(tableName,
					//noteId不用管
					createTime to write_nowTime,
					
					contentType to content.typeTag,
					contentString to write_noteContentString,
					contentUpdateTime to write_nowTime,
					
					memoryState to write_memory.state.name,
					memoryProgress to write_memory.progress,
					reviewTime to write_memory.reviewTime,
					memoryLoad to write_memory.load,
					memoryUpdateTime to write_nowTime
				)
				if (newNoteId == -1L) throw InternalNotebookException("failed to insert new note")
			}
			uniqueTagTable.run {
				content.uniqueTags.forEach { tag ->
					insert(tableName,
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
			delete(notesTable.tableName, notesTable.noteId + "=" + noteId, null)
			delete(uniqueTagTable.tableName, uniqueTagTable.noteId + "=" + noteId, null)
			delete(searchTagTable._name, searchTagTable.noteId + "=" + noteId, null)
		}
	}
	
	override fun modifyNoteContent(noteId: Long, content: NoteContent) {
		//检查改动后是否会产生冲突
		throwIfDuplicated(content.uniqueTags,noteId)
		
		database.transaction {
			//删除原有的 uniqueTags 和 searchTags
			uniqueTagTable.run { delete(tableName,"${uniqueTagTable.noteId} = $noteId") }
			searchTagTable.run { delete(_name,"${searchTagTable.noteId} = $noteId") }
			
			//添加note
			notesTable.run {
				val newContentCoder = noteContentCoders[content.typeTag] ?:
					throw NoteTypeNotSupportedException(content.typeTag)
				val nowTime = System.currentTimeMillis()
				
				update(tableName,
					contentType to content.typeTag,
					contentString to newContentCoder.encode(content),
					contentUpdateTime to nowTime)
					.whereArgs("${notesTable.noteId} = $noteId")
					.exec()
			}
			
			//添加新的 uniqueTags 和 searchTags
			uniqueTagTable.run {
				content.uniqueTags.forEach { tag ->
					insert(tableName,
						uniqueTagTable.noteId to noteId,
						uniqueTagTable.uniqueTag to tag
					)
				}
			}
			searchTagTable.run {
				content.searchTags.forEach { tag ->
					database.insert(_name,
						searchTagTable.noteId to noteId,
						searchTag to tag
					)
				}
			}
		}
	}
	
	override fun modifyNoteMemory(noteId: Long, memory: NoteMemory) {
		val nowTime = System.currentTimeMillis()
		notesTable.run {
			database.update(tableName,
				memoryState to memory.state.name,
				memoryProgress to memory.progress,
				memoryLoad to memory.load,
				reviewTime to memory.reviewTime,
				memoryUpdateTime to nowTime
			)
		}
	}
}


