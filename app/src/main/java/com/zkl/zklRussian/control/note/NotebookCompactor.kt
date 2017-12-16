package com.zkl.zklRussian.control.note

import android.database.sqlite.SQLiteDatabase
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Notebook
import com.zkl.zklRussian.core.note.base.NotebookMemoryStatus
import java.io.File

interface NotebookCompactor {
	fun createNotebook(file: File, notebookName: String): MutableNotebook? = null
	fun loadBrief(file: File): NotebookBrief? = loadNotebook(file)?.use { notebook ->
		NotebookBrief(file, notebook.name,
			notebook.memoryState.status != NotebookMemoryStatus.infant)
	}
	fun loadNotebook(file: File): Notebook? = null
	fun loadMutableNotebook(file: File): MutableNotebook? = null
	fun deleteNotebook(file: File): Boolean = false
}

class NotCreatableException(file: File)
	: Exception("Creating notebook in file ${file.path} failed.")

class FileNotCompatibleException(file: File)
	: Exception("Can not load the file ${file.path} as a Notebook.")


//versioned map

private val notebookCompactors by lazy {
	arrayListOf(
		Notebook3Compactor()
	)
}

object MainCompactor : NotebookCompactor {
	
	override fun createNotebook(file: File, notebookName: String): MutableNotebook?
		= notebookCompactors.asSequence()
		.mapNotNull { it.createNotebook(file, notebookName) }
		.firstOrNull()
	
	fun createNotebookOrThrow(file: File, notebookName: String): MutableNotebook
		= createNotebook(file, notebookName) ?: throw NotCreatableException(file)
	
	override fun loadNotebook(file: File): Notebook?
		= notebookCompactors.asSequence()
		.mapNotNull { it.loadNotebook(file) }
		.firstOrNull()
	
	fun loadNotebookOrThrow(file: File): Notebook
		= loadNotebook(file) ?: throw FileNotCompatibleException(file)
	
	override fun loadMutableNotebook(file: File): MutableNotebook?
		= notebookCompactors.asSequence()
		.mapNotNull { it.loadMutableNotebook(file) }
		.firstOrNull()
	
	fun loadMutableNotebookOrThrow(file: File): MutableNotebook
		= loadMutableNotebook(file) ?: throw FileNotCompatibleException(file)
	
	override fun deleteNotebook(file: File): Boolean
		= notebookCompactors.asSequence()
		.map { it.deleteNotebook(file) }
		.any { it }
	
}

//versioned class

class Notebook3Compactor : NotebookCompactor {
	
	override fun createNotebook(file: File, notebookName: String): MutableNotebook? {
		val database = SQLiteDatabase.openOrCreateDatabase(file, null)
		val notebook = Notebook3(database)
		notebook.createTables(notebookName)
		return notebook
	}
	
	override fun loadNotebook(file: File): Notebook? {
		val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
		val notebook = Notebook3(database)
		return if (notebook.checkTables()) notebook else null
	}
	
	override fun loadMutableNotebook(file: File): MutableNotebook? {
		val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE)
		val notebook = Notebook3(database)
		return if (notebook.checkTables()) notebook else {
			notebook.close()
			null
		}
	}
	
	override fun deleteNotebook(file: File): Boolean {
		return loadNotebook(file)?.let {
			it.close()
			SQLiteDatabase.deleteDatabase(file)
		} ?: false
	}
	
}


