package com.zkl.memruss.control.note

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.zkl.memruss.control.tools.orderBy
import com.zkl.memruss.control.tools.stringData.StringData
import com.zkl.memruss.core.note.Note
import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.Notebook
import com.zkl.memruss.core.note.QuestionContent
import com.zkl.memruss.core.note.base.*
import org.jetbrains.anko.db.*

private object BookVersionTable {
	internal val tableName = "BookVersion"
	
	val versionCode = "versionCode"
	val typeCode = "typeCode"
}

private object ManifestsTable {
	val tableName = "Manifests"
	
	val bookName = "bookName"
	val bookInfo = "bookInfo"
	val memoryPlanArgs = "memoryPlanArgs"
	val memoryPlanProgress = "memoryPlanProgress"
	val rawData = "raw"
	
}

private object BookTable {
	val tableName = "book"
	
	val noteId = "id"
	val modifyTime = "modifyTime"
	val noteData = "bookInfo"
	val noteRawData = "raw"
	val searchTags = "searchTags"
	val duplicateTags = "duplicateTags"
	val progress = "progress"
	val nextTime = "nextTime"
	
	val standardColumns = arrayOf(noteId, modifyTime, noteData, noteRawData, searchTags, duplicateTags, progress, nextTime)
}

class Notebook2(val database: SQLiteDatabase) : Notebook {
	
	companion object {
		val VERSION = 2
	}
	
	//life cycle
	fun checkVersion(): Boolean {
		return try {
			val version = BookVersionTable.run {
				database.select(tableName, versionCode)
					.exec {
						moveToFirst()
						getInt(0)
					}
			}
			version == VERSION
		} catch (e: Exception) {
			false
		}
	}
	
	override fun close() {
		database.close()
	}
	
	
	//info
	override val version: Int = VERSION
	
	override val name: String
		get() = ManifestsTable.run {
			database.select(tableName, bookName)
				.exec {
					moveToFirst()
					getString(0)
				}
		}
	
	
	//getters
	override val noteCount: Int
		get() = BookTable.run {
			database.select(tableName, "count(*)").exec {
				moveToFirst()
				getInt(0)
			}
		}
	
	override fun getNote(noteId: Long): Note {
		return database.selectNotes()
			.whereArgs(BookTable.noteId + "=" + noteId)
			.exec { parseNoteList() }.firstOrNull()
			?: throw NoteIdNotFoundException(noteId)
	}
	
	override fun rawGetNotes(count: Int, offset: Int): List<Note> {
		return database.selectNotes()
			.limit(offset, count)
			.exec { parseNoteList() }
	}
	
	override fun selectLatestNotes(count: Int, offset: Int): List<Note> {
		return database.selectNotes()
			.orderBy(BookTable.modifyTime, SqlOrderDirection.DESC)
			.limit(offset, count)
			.exec { parseNoteList() }
	}
	
	override fun checkUniqueTag(uniqueTag: String, exceptId: Long): Long {
		return database.selectNotes()
			.whereArgs("${BookTable.noteId} != $exceptId and ${BookTable.searchTags} like '%|$uniqueTag%'")
			.exec { parseNoteList().firstOrNull()?.id ?: -1L }
	}
	
	override fun selectByKeyword(keyword: String, count: Int, offset: Int): List<Note> {
		return database.selectNotes()
			.whereArgs("${BookTable.searchTags} like '%$keyword%'")
			.limit(offset, count)
			.exec { parseNoteList() }
	}
	
	//private
	private fun SQLiteDatabase.selectNotes(): SelectQueryBuilder {
		return BookTable.run {
			this@selectNotes.select(tableName, *standardColumns)
		}
	}
	
	private fun Cursor.parseNoteList(): List<Note> = parseList(rowParser { noteId: Long, modifyTime: Long, noteData: String, _: Any?, _: String, _: String, progress: Int, nextTime: Long ->
		
		val stringData = StringData.decode(noteData).getStringData(2)
		val noteContent = QuestionContent(
			question = stringData.getString(0),
			answer = stringData.getString(1))
		
		val memoryState = when (progress) {
			-1 -> NoteMemoryState.infantState()
			-2 -> {
				val newProgress = 20.0
				val interval = MemoryAlgorithm.computeReviewInterval(newProgress)
				val load = MemoryAlgorithm.computeLoad(interval)
				val nowTime = System.currentTimeMillis()
				NoteMemoryState(NoteMemoryStatus.LEARNING, newProgress, load, nowTime + interval)
			}
			else -> {
				val newProgress = progress.toDouble()/100.0
				val interval = MemoryAlgorithm.computeReviewInterval(newProgress)
				val load = MemoryAlgorithm.computeLoad(interval)
				NoteMemoryState(NoteMemoryStatus.LEARNING, newProgress, load, nextTime)
			}
		}
		
		object : Note {
			override val id: Long = noteId
			override val createTime: Long = modifyTime
			
			override val content: NoteContent = noteContent
			override val contentUpdateTime: Long = modifyTime
			
			override val memoryState: NoteMemoryState = memoryState
			override val memoryUpdateTime: Long = modifyTime
		}
	})
	
	
	//memory
	override val memoryState: NotebookMemoryState
		get()  {
			val progressString = database.select(ManifestsTable.tableName, ManifestsTable.memoryPlanProgress)
				.exec {
					moveToFirst()
					getString(0)
				}
			val stringData = StringData.decode(progressString)
			
			val status = when (stringData.getInteger(0)) {
				0 -> NotebookMemoryStatus.LEARNING
				else -> NotebookMemoryStatus.INFANT
			}
			return NotebookMemoryState(
				status = status,
				planLaunchTime = stringData.getLong(2),
				lastActivateTime = stringData!!.getLong(3)
			)
		}
	override val memoryPlan: MemoryPlan?
		get() {
			val planString = database.select(ManifestsTable.tableName, ManifestsTable.memoryPlanArgs)
				.exec {
					moveToFirst()
					getString(0)
				}
			val stringData = StringData.decode(planString)
			val workLoadLimit = stringData.getFloat(0)
			val refillInterval = stringData.getLong(1)
			return MemoryPlan(
				workLoadLimit.toDouble(),
				24.0 * 3600 * 1000 / refillInterval
			)
		}
	
	override fun selectNeedActivateNoteIds(asc: Boolean, count: Int, offset: Int): List<Long> {
		return database.selectNotes()
			.whereArgs("${BookTable.progress} = -1")
			.orderBy(BookTable.modifyTime, asc)
			.limit(offset, count)
			.exec { parseNoteList().map { it.id } }
	}
	
	override fun countNeedReviewNotes(nowTime: Long): Int {
		return BookTable.run { database.select(tableName,"count(*)")
			.whereArgs("$progress>-1 AND $nextTime < $nowTime ")
			.exec {
				moveToFirst()
				getInt(0)
			}
		}
	}
	
	override fun selectNeedReviewNotes(nowTime: Long, asc: Boolean, count: Int, offset: Int): List<Note> {
		return database.selectNotes()
			.whereArgs("${BookTable.nextTime} < $nowTime")
			.limit(offset, count)
			.exec { parseNoteList() }
	}
	
	override fun sumMemoryLoad(): Double {
		return rawGetAllNotes().sumByDouble { it.memoryState.load }
	}
	
}