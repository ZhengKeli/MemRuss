package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDialogFragment
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Note
import com.zkl.zklRussian.core.note.Notebook
import org.jetbrains.anko.bundleOf

internal fun Bundle.getNotebookKey(key: String): NotebookKey? = getSerializable(key) as? NotebookKey
internal fun Bundle.putNotebookKey(key: String, value: NotebookKey?) = putSerializable(key, value)
internal operator fun Bundle?.plus(bundle: Bundle) = (this?:Bundle()).apply { putAll(bundle) }

abstract class NotebookHoldingFragment: Fragment() {
	
	companion object {
		val arg_notebookKey = "notebookKey"
		fun <T : NotebookHoldingFragment> Class<T>.newInstance(notebookKey: NotebookKey):T
			= newInstance().apply {
			arguments += bundleOf(arg_notebookKey to notebookKey)
		}
	}
	
	protected var notebookKey: NotebookKey
		get()= arguments!!.getNotebookKey(arg_notebookKey)!!
		set(value) {
			arguments!!.putNotebookKey(arg_notebookKey,value)
			cachedNotebook = null
		}
	
	private var cachedNotebook: Notebook? = null
	protected val notebook: Notebook
		get() = cachedNotebook ?:loadNotebook()
	protected val mutableNotebook: MutableNotebook get() = notebook as MutableNotebook
	fun loadNotebook() = myApp.notebookShelf.restoreNotebook(notebookKey).also { cachedNotebook = it }
	fun tryLoadNotebook():Notebook?{
		return try {
			loadNotebook()
		} catch (e: Exception) {
			null
		}
	}
}
abstract class NoteHoldingFragment : NotebookHoldingFragment() {
	
	companion object {
		val arg_noteId = "noteId"
		fun <T : NoteHoldingFragment> Class<T>.newInstance(notebookKey: NotebookKey, noteId: Long)
			= newInstance(notebookKey).apply {
			arguments += bundleOf(arg_noteId to noteId)
		}
	}

	protected var noteId: Long
		get() = arguments?.getLong(arg_noteId, -1L) ?: -1L
		set(value) {
			arguments!!.putLong(arg_noteId, value)
			cachedNote = null
		}
	
	private var cachedNote: Note? = null
	val note:Note get() = cachedNote?: loadNote()
	
	fun loadNote() = notebook.getNote(noteId).also { cachedNote = it }
	fun tryLoadNote():Note?{
		return try {
			loadNote()
		} catch (e: Exception) {
			null
		}
	}
}

abstract class NotebookHoldingDialog: AppCompatDialogFragment(){
	
	companion object {
		val arg_notebookKey = "notebookKey"
		fun <T : NotebookHoldingDialog> Class<T>.newInstance(notebookKey: NotebookKey):T
			= newInstance().apply {
			arguments += bundleOf(arg_notebookKey to notebookKey)
		}
	}
	
	protected var notebookKey: NotebookKey
		get()= arguments!!.getNotebookKey(NotebookHoldingFragment.arg_notebookKey)!!
		set(value) {
			arguments!!.putNotebookKey(NotebookHoldingFragment.arg_notebookKey,value)
			cachedNotebook = null
		}
	
	private var cachedNotebook: Notebook? = null
	protected val notebook: Notebook
		get() = cachedNotebook ?:myApp.notebookShelf.restoreNotebook(notebookKey).also { cachedNotebook = it }
	protected val mutableNotebook: MutableNotebook get() = notebook as MutableNotebook
	
}
abstract class NoteHoldingDialog: NotebookHoldingDialog(){
	
	companion object {
		val arg_noteId = "noteId"
		fun <T : NoteHoldingDialog> Class<T>.newInstance(notebookKey: NotebookKey, noteId: Long)
			= newInstance(notebookKey).apply {
			arguments += bundleOf(arg_noteId to noteId)
		}
	}
	
	protected var noteId: Long
		get() = arguments?.getLong(NoteHoldingFragment.arg_noteId, -1L) ?: -1L
		set(value) {
			arguments!!.putLong(NoteHoldingFragment.arg_noteId, value)
			cachedNote = null
		}
	
	private var cachedNote: Note? = null
	val note:Note get() = cachedNote?:notebook.getNote(noteId).also { cachedNote=it }
	
}
