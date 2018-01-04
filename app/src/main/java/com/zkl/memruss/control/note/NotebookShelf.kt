package com.zkl.memruss.control.note

import com.zkl.memruss.control.tools.HookSystem
import com.zkl.memruss.core.note.MutableNotebook
import com.zkl.memruss.core.note.Notebook
import java.io.File
import java.io.Serializable
import java.util.*

class NotebookShelf(workingDir: File){
	
	init { workingDir.mkdirs() }
	private val booksDir = workingDir.resolve("books").apply { mkdirs() }
	
	//brief
	fun loadNotebookBriefs(): List<NotebookBrief> {
		return booksDir.listFiles { _, name -> !name.endsWith("-journal") }
			.mapNotNull { MainCompactor.loadBriefOrNull(it) }
	}
	
	
	//book opening & creating
	private val openedNotebooks = HookSystem<NotebookKey, Notebook>()
	@Synchronized fun createNotebook(notebookName: String): Pair<NotebookKey, MutableNotebook> {
		val file: File = generateNotebookFile()
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
		try { return openMutableNotebook(file) }catch (e:Exception){}
		return openReadOnlyNotebook(file)
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
	@Synchronized fun importNotebook(file: File): Pair<NotebookKey, Notebook> {
		MainCompactor.loadReadOnlyNotebookOrThrow(file)
		val target = generateNotebookFile()
		file.copyTo(target)
		return openNotebook(target)
	}
	@Synchronized fun exportNotebook(file: File, target: File) {
		target.parentFile.let { if (!it.exists()) it.mkdirs() }
		file.copyTo(target,true)
	}
	
	private fun generateNotebookFile(): File {
		//find a new file name
		val random = Random()
		var randomFile: File
		do {
			val randomFileName = Math.abs(random.nextLong()).toString()
			randomFile = File(booksDir, randomFileName)
		} while (randomFile.exists())
		return randomFile
	}
	
}

data class NotebookBrief(val file: File, val bookName: String, val hasPlan: Boolean, val mutable: Boolean) : Serializable
data class NotebookKey(val canonicalPath: String, val mutable: Boolean) : Serializable
