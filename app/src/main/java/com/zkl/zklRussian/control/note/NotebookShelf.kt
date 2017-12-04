package com.zkl.zklRussian.control.note

import android.database.sqlite.SQLiteDatabase
import com.zkl.zklRussian.control.tools.HookSystem
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Notebook
import java.io.File
import java.io.Serializable
import java.util.*

class NotebookShelf(workingDir: File){
	
	init { workingDir.mkdirs() }
	private val booksDir = workingDir.resolve("books").apply { mkdirs() }
	
	//summary
	fun loadBookSummaries(): List<NotebookBrief> {
		return booksDir.takeIf { it.exists() }
			?.listFiles { _, name -> name.endsWith(".zrb") }
			?.mapNotNull { loadBookSummary(it) }
			?: emptyList()
	}
	private fun loadBookSummary(file: File): NotebookBrief? {
		return try {
			val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
			MutableNotebook3(database).use { notebook -> NotebookBrief(file, notebook.name) }
		}catch (e:Exception){
			null
		}
	}
	
	
	//book opening & creating
	private val openedNotebooks = HookSystem<NotebookKey, Notebook>()
	@Synchronized fun createNotebook(bookName: String): Pair<NotebookKey, MutableNotebook> {
		val file: File = kotlin.run {
			//find a new file name
			val random = Random()
			var randomFile: File
			do {
				val randomFileName = bookName + "_" + Math.abs(random.nextLong()) + ".zrb"
				randomFile = File(booksDir, randomFileName)
			} while (randomFile.exists())
			return@run randomFile
		}
		val database = SQLiteDatabase.openDatabase(file.path, null,
			SQLiteDatabase.CREATE_IF_NECESSARY or SQLiteDatabase.OPEN_READWRITE)
		val notebook = MutableNotebook3(database).apply { createTables(bookName) }
		val key = NotebookKey(file.canonicalPath, true)
		openedNotebooks[key] = notebook
		return Pair(key, notebook)
	}
	@Synchronized fun openNotebook(file:File): Pair<NotebookKey, Notebook> {
		val key = NotebookKey(file.canonicalPath, false)
		val opened = openedNotebooks[key]
		if (opened is Notebook) return Pair(key,opened)
		
		val database = SQLiteDatabase.openDatabase(key.canonicalPath, null, SQLiteDatabase.OPEN_READONLY)
		val notebook = MutableNotebook3(database) //todo change to immutableNotebook
		openedNotebooks[key] = notebook
		return Pair(key,notebook)
	}
	@Synchronized fun openMutableNotebook(file: File): Pair<NotebookKey, MutableNotebook> {
		val key = NotebookKey(file.canonicalPath, true)
		val opened = openedNotebooks[key]
		if (opened is MutableNotebook) return Pair(key,opened)
		
		val database = SQLiteDatabase.openDatabase(key.canonicalPath, null, SQLiteDatabase.OPEN_READWRITE)
		val mutableNotebook = MutableNotebook3(database)
		openedNotebooks[key] = mutableNotebook
		return Pair(key,mutableNotebook)
	}
	@Synchronized fun restoreNotebook(key: NotebookKey): Notebook {
		return openedNotebooks[key] ?:
			if (key.mutable) openMutableNotebook(File(key.canonicalPath)).second
			else openNotebook(File(key.canonicalPath)).second
	}
	@Synchronized fun deleteNotebook(file: File): Boolean {
		return SQLiteDatabase.deleteDatabase(file)
	}
	
}

data class NotebookBrief(val file: File, val bookName: String): Serializable
data class NotebookKey internal constructor(val canonicalPath: String, val mutable: Boolean) : Serializable
