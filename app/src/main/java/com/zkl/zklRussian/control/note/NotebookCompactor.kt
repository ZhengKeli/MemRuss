package com.zkl.zklRussian.control.note

import com.zkl.zklRussian.core.note.MutableNotebook
import com.zkl.zklRussian.core.note.Notebook
import java.io.File

interface NotebookCompactor {
	fun loadNotebook(file: File): Notebook
}

interface MutableNotebookCompactor : NotebookCompactor {
	override fun loadNotebook(file: File): Notebook = loadMutableNotebook(file)
	fun loadMutableNotebook(file: File): MutableNotebook
}

class FileNotCompatibleException(file: File)
	:Exception("Can not load the file ${file.path} as a Notebook.")

