package com.zkl.zklRussian.control.note

import android.database.sqlite.SQLiteDatabase
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Notebook
import java.io.File
import java.io.Serializable
import java.util.*

class NotebookShelf(workingDir: File){
	init {
		workingDir.mkdirs()
	}
	val booksDir = workingDir.resolve("books").apply { mkdirs() }
	
	
	//summary
	data class NotebookSummary(val file: File, val bookName: String):Serializable
	fun loadBookSummaries(): List<NotebookSummary> {
		return booksDir.takeIf { it.exists() }
			?.listFiles { _, name -> name.endsWith(".zrb") }
			?.mapNotNull { loadBookSummary(it) }
			?: emptyList()
	}
	private fun loadBookSummary(file: File): NotebookSummary? {
		try {
			val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
			MutableNotebook3(database).use { notebook ->
				return NotebookSummary(file, notebook.name)
			}
		}catch (e:Exception){
			return null
		}
	}
	
	
	//book opening & creating
	@Synchronized fun openNotebook(file: File): Notebook {
		val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
		return MutableNotebook3(database)
	}
	@Synchronized fun openMutableNotebook(file: File): MutableNotebook {
		val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE)
		return MutableNotebook3(database)
	}
	@Synchronized fun createNotebook(bookName: String): MutableNotebook {
		//find a new file name
		val file:File = kotlin.run {
			val random = Random()
			var randomFile:File
			do {
				val randomFileName = bookName+"_"+ Math.abs(random.nextLong())+".zrb"
				randomFile = File(booksDir, randomFileName)
			}while (randomFile.exists())
			return@run randomFile
		}
		
		val database = SQLiteDatabase.openDatabase(file.path, null,
			SQLiteDatabase.CREATE_IF_NECESSARY or SQLiteDatabase.OPEN_READWRITE)
		val notebook = MutableNotebook3(database)
		notebook.createTables(bookName)
		return notebook
	}
	
}


