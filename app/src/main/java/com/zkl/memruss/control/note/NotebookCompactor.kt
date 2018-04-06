package com.zkl.memruss.control.note

import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import com.zkl.memruss.core.note.MutableNotebook
import com.zkl.memruss.core.note.Notebook
import com.zkl.memruss.core.note.base.isLearning
import com.zkl.memruss.utils.tryOrNull
import java.io.File

interface NotebookCompactor {
	
	companion object {
		val LATEST_VERSION get() = MutableNotebook3.VERSION
		val defaultImportDir get() = Environment.getExternalStorageDirectory().resolve("ZKLRussian")
		val defaultExportDir get() = Environment.getExternalStorageDirectory().resolve("MemRuss")
	}
	
	//create
	fun createNotebook(file: File, notebookName: String): MutableNotebook?
	
	fun createNotebookOrNull(file: File, notebookName: String): MutableNotebook?
		= tryOrNull { createNotebook(file, notebookName) }
	
	fun createNotebookOrThrow(file: File, notebookName: String): MutableNotebook
		= createNotebook(file, notebookName) ?: throw NotCreatableException(file)
	
	//load: read only
	fun loadReadOnlyNotebook(file: File): Notebook?
	
	fun loadReadOnlyNotebookOrNull(file: File): Notebook?
		= tryOrNull { loadReadOnlyNotebook(file) }
	
	fun loadReadOnlyNotebookOrThrow(file: File): Notebook
		= loadReadOnlyNotebook(file) ?: throw FileNotCompatibleException(file)
	
	//load: mutable
	fun loadMutableNotebook(file: File): MutableNotebook?
	
	fun loadMutableNotebookOrNull(file: File): MutableNotebook?
		= tryOrNull { loadMutableNotebook(file) }
	
	fun loadMutableNotebookOrThrow(file: File): MutableNotebook
		= loadMutableNotebook(file) ?: throw FileNotCompatibleException(file)
	
	//load: as possible
	fun loadNotebookOrNull(file: File): Notebook?
		= loadMutableNotebookOrNull(file) ?: loadReadOnlyNotebookOrNull(file)
	
	fun loadNotebookOrThrow(file: File): Notebook
		= loadMutableNotebookOrNull(file) ?: loadReadOnlyNotebookOrThrow(file)
	
	fun loadBrief(file: File): NotebookBrief? {
		return loadNotebookOrNull(file)?.use { notebook ->
			NotebookBrief(file, notebook.name, notebook.isLearning, notebook is MutableNotebook)
		}
	}
	
	fun loadBriefOrNull(file: File): NotebookBrief? = tryOrNull { loadBrief(file) }
	
	fun deleteNotebook(file: File): Boolean
	
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
		.mapNotNull { it.createNotebookOrNull(file, notebookName) }
		.firstOrNull()
	
	override fun loadReadOnlyNotebook(file: File): Notebook?
		= notebookCompactors.asSequence()
		.mapNotNull { it.loadReadOnlyNotebookOrNull(file) }
		.firstOrNull()
	
	override fun loadMutableNotebook(file: File): MutableNotebook?
		= notebookCompactors.asSequence()
		.mapNotNull { it.loadMutableNotebookOrNull(file) }
		.firstOrNull()
	
	override fun deleteNotebook(file: File): Boolean
		= notebookCompactors.asSequence()
		.map { it.deleteNotebook(file) }
		.any { it }
}


//versioned class

class MutableNotebook3Compactor : NotebookCompactor {
	
	companion object {
		val fileExtension = "mnb"
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
		if (!notebook.checkVersion()) {
			notebook.close()
			return null
		}
		return notebook
	}
	
	override fun loadMutableNotebook(file: File): MutableNotebook? {
		val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE)
		val notebook = MutableNotebook3(database)
		if (!notebook.checkVersion()) {
			notebook.close()
			return null
		}
		return notebook
	}
	
	override fun deleteNotebook(file: File): Boolean {
		return loadReadOnlyNotebookOrNull(file)?.let {
			it.close()
			SQLiteDatabase.deleteDatabase(file)
		} ?: false
	}
	
}

class Notebook2Compactor : NotebookCompactor {
	
	companion object {
		val fileExtension = "zrb"
	}
	
	override fun createNotebook(file: File, notebookName: String): MutableNotebook? = null
	
	override fun loadMutableNotebook(file: File): MutableNotebook? = null
	
	override fun loadReadOnlyNotebook(file: File): Notebook? {
		val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
		val notebook = Notebook2(database)
		if (!notebook.checkVersion()) {
			notebook.close()
			return null
		}
		return notebook
	}
	
	override fun deleteNotebook(file: File): Boolean {
		return loadReadOnlyNotebookOrNull(file)?.let {
			it.close()
			SQLiteDatabase.deleteDatabase(file)
		} ?: false
	}
}
