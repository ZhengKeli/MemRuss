package com.zkl.memruss.control.note

import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import com.zkl.memruss.core.note.MutableNotebook
import com.zkl.memruss.core.note.Notebook
import com.zkl.memruss.core.note.base.isLearning
import java.io.File

interface NotebookCompactor {
	
	companion object {
		val LATEST_VERSION get() = MutableNotebook3.VERSION
		val defaultImportDir get() = Environment.getExternalStorageDirectory().resolve("ZKLRussian")
		val defaultExportDir get() = Environment.getExternalStorageDirectory().resolve("MemRuss")
	}
	
	fun createNotebook(file: File, notebookName: String): MutableNotebook? = null
	
	fun createNotebookOrThrow(file: File, notebookName: String): MutableNotebook
		= createNotebook(file, notebookName) ?: throw NotCreatableException(file)
	
	fun loadReadOnlyNotebook(file: File): Notebook? = null
	
	fun loadReadOnlyNotebookOrThrow(file: File): Notebook
		= loadReadOnlyNotebook(file) ?: throw FileNotCompatibleException(file)
	
	fun loadMutableNotebook(file: File): MutableNotebook? = null
	
	fun loadMutableNotebookOrThrow(file: File): MutableNotebook
		= loadMutableNotebook(file) ?: throw FileNotCompatibleException(file)
	
	fun loadNotebook(file: File): Notebook?
		= loadMutableNotebook(file) ?: loadReadOnlyNotebook(file)
	
	fun loadNotebookOrThrow(file: File): Notebook
		= loadNotebook(file) ?: throw FileNotCompatibleException(file)
	
	fun loadBrief(file: File): NotebookBrief?
		= loadNotebook(file)?.use { notebook ->
		NotebookBrief(file, notebook.name, notebook.isLearning, notebook is MutableNotebook)
	}
	
	fun deleteNotebook(file: File): Boolean = false
	
}

class NotCreatableException(file: File)
	: Exception("Creating notebook in file ${file.path} failed.")

class FileNotCompatibleException(file: File)
	: Exception("Can not load the file ${file.path} as a Notebook.")



//versioned map

private val notebookCompactors by lazy {
	arrayListOf(
		MutableNotebook3Compactor(),
		Notebook2Compactor()
	)
}

object MainCompactor : NotebookCompactor {
	
	override fun createNotebook(file: File, notebookName: String): MutableNotebook?
		= notebookCompactors.asSequence()
		.mapNotNull { it.createNotebook(file, notebookName) }
		.firstOrNull()
	
	override fun loadReadOnlyNotebook(file: File): Notebook?
		= notebookCompactors.asSequence()
		.mapNotNull { it.loadReadOnlyNotebook(file) }
		.firstOrNull()
	
	override fun loadMutableNotebook(file: File): MutableNotebook?
		= notebookCompactors.asSequence()
		.mapNotNull { it.loadMutableNotebook(file) }
		.firstOrNull()
	
	override fun deleteNotebook(file: File): Boolean
		= notebookCompactors.asSequence()
		.map { it.deleteNotebook(file) }
		.any { it }
}



//versioned class

class MutableNotebook3Compactor : NotebookCompactor {
	
	companion object {
		val fileExtension = ".mnb"
	}
	
	override fun createNotebook(file: File, notebookName: String): MutableNotebook? {
		val database = SQLiteDatabase.openOrCreateDatabase(file, null)
		val notebook = MutableNotebook3(database)
		notebook.createTables(notebookName)
		return notebook
	}
	
	override fun loadReadOnlyNotebook(file: File): Notebook? {
		val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
		val notebook = MutableNotebook3(database)
		return if (notebook.checkVersion()) notebook else null
	}
	
	override fun loadMutableNotebook(file: File): MutableNotebook? {
		val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE)
		val notebook = MutableNotebook3(database)
		return if (notebook.checkVersion()) notebook else {
			notebook.close()
			null
		}
	}
	
	override fun deleteNotebook(file: File): Boolean {
		return loadReadOnlyNotebook(file)?.let {
			it.close()
			SQLiteDatabase.deleteDatabase(file)
		} ?: false
	}
	
}

class Notebook2Compactor : NotebookCompactor {
	
	companion object {
		val fileExtension = ".zrb"
	}
	
	override fun loadReadOnlyNotebook(file: File): Notebook? {
		val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
		val notebook = Notebook2(database)
		return if (notebook.checkVersion()) notebook else null
	}
	
	override fun deleteNotebook(file: File): Boolean {
		return loadReadOnlyNotebook(file)?.let {
			it.close()
			SQLiteDatabase.deleteDatabase(file)
		} ?: false
	}
}
