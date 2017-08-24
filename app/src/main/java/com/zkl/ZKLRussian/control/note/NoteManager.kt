package com.zkl.ZKLRussian.control.note

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.support.v4.app.Fragment
import android.util.SparseArray
import com.zkl.ZKLRussian.control.myApp
import com.zkl.ZKLRussian.core.note.MutableNotebook
import com.zkl.ZKLRussian.core.note.Notebook
import java.io.File
import java.io.Serializable
import java.util.*

class NoteManager(workingDir: File){
	init {
		workingDir.mkdirs()
	}
	val booksDir = workingDir.resolve("books").apply { mkdirs() }
	
	
	//summary
	data class NotebookSummary(val file: File, val bookName: String):Serializable
	fun loadBookSummaries(): Collection<NotebookSummary> {
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
	
	
	//register
	private val registeredNotebooks = SparseArray<Notebook>()
	@Synchronized fun registerNotebook(notebook: Notebook): Int {
		val key = notebook.hashCode()
		if (registeredNotebooks.get(key) == null)
			registeredNotebooks.put(notebook.hashCode(), notebook)
		return key
	}
	@Synchronized fun unregisterNotebook(key: Int) {
		registeredNotebooks.remove(key)
	}
	@Synchronized fun getRegisterNotebook(key: Int): Notebook? {
		return registeredNotebooks.get(key)
	}
	
}


val Activity.noteManager: NoteManager get() = myApp.noteManager
val Fragment.noteManager: NoteManager get() = myApp.noteManager


