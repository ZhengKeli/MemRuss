package com.zkl.zklRussian.control.note

import com.zkl.zklRussian.control.tools.HookSystem
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Notebook
import java.io.File
import java.io.Serializable
import java.util.*

class NotebookShelf(workingDir: File){
	
	init { workingDir.mkdirs() }
	private val booksDir = workingDir.resolve("books").apply { mkdirs() }
	
	//brief
	fun loadNotebookBriefs(): List<NotebookBrief> {
		return booksDir.takeIf { it.exists() }
			?.listFiles { _, name -> name.endsWith(".zrb") }
			?.mapNotNull { loadNotebookBrief(it) }
			?: emptyList()
	}
	private fun loadNotebookBrief(file: File): NotebookBrief? {
		return MainCompactor.loadBrief(file)
	}
	
	
	//book opening & creating
	private val openedNotebooks = HookSystem<NotebookKey, Notebook>()
	@Synchronized fun createNotebook(notebookName: String): Pair<NotebookKey, MutableNotebook> {
		val file: File = generateNotebookFile(notebookName)
		val notebook = MainCompactor.createNotebookOrThrow(file, notebookName)
		val key = NotebookKey(file.canonicalPath, true)
		openedNotebooks[key] = notebook
		return Pair(key, notebook)
	}
	@Synchronized fun openReadOnlyNotebook(file:File): Pair<NotebookKey, Notebook> {
		val key = NotebookKey(file.canonicalPath, false)
		val opened = openedNotebooks[key]
		if (opened is Notebook) return Pair(key,opened)
		val notebook = MainCompactor.loadReadOnlyNotebookOrThrow(file)
		openedNotebooks[key] = notebook
		return Pair(key,notebook)
	}
	@Synchronized fun openMutableNotebook(file:File): Pair<NotebookKey, MutableNotebook>{
		val key = NotebookKey(file.canonicalPath, true)
		val opened = openedNotebooks[key]
		if (opened is MutableNotebook) return Pair(key,opened)
		val notebook = MainCompactor.loadMutableNotebookOrThrow(file)
		openedNotebooks[key] = notebook
		return Pair(key,notebook)
	}
	@Synchronized fun openNotebook(file:File): Pair<NotebookKey, Notebook> {
		return try {
			openMutableNotebook(file)
		}catch (e:Exception){
			null
		}?:openReadOnlyNotebook(file)
	}
	@Synchronized fun restoreNotebook(key: NotebookKey): Notebook {
		return openedNotebooks[key] ?:
			if (key.mutable) openMutableNotebook(File(key.canonicalPath)).second
			else openReadOnlyNotebook(File(key.canonicalPath)).second
	}
	@Synchronized fun deleteNotebook(file: File): Boolean {
		NotebookKey(file.canonicalPath, true).let {
			if(openedNotebooks[it]!=null) return deleteNotebook(it)
		}
		NotebookKey(file.canonicalPath, false).let {
			if(openedNotebooks[it]!=null) return deleteNotebook(it)
		}
		return MainCompactor.deleteNotebook(file)
	}
	@Synchronized fun deleteNotebook(key: NotebookKey): Boolean {
		openedNotebooks[key]?.close()
		openedNotebooks.remove(key)
		return MainCompactor.deleteNotebook(File(key.canonicalPath))
	}
	@Synchronized fun importNotebook(file: File): Pair<NotebookKey, Notebook>{
		val brief = loadNotebookBrief(file)?: throw FileNotCompatibleException(file)
		val target = generateNotebookFile(brief.bookName)
		file.copyTo(target)
		return openNotebook(target)
	}
	@Synchronized fun exportNotebook(file: File, target: File) {
		target.parentFile.let { if (!it.exists()) it.mkdirs() }
		file.copyTo(target,true)
	}
	
	private fun generateNotebookFile(bookName: String): File {
		//find a new file name
		val random = Random()
		var randomFile: File
		do {
			val randomFileName = bookName + "_" + Math.abs(random.nextLong()) + ".zrb"
			randomFile = File(booksDir, randomFileName)
		} while (randomFile.exists())
		return randomFile
	}
	
}

data class NotebookBrief(val file: File, val bookName: String, val hasPlan: Boolean, val mutable: Boolean) : Serializable
data class NotebookKey(val canonicalPath: String, val mutable: Boolean) : Serializable
