package com.zkl.zklRussian.ui

import android.support.v4.app.Fragment
import com.zkl.zklRussian.control.note.NotebookShelf
import org.jetbrains.anko.bundleOf

class NotebookExportFragment : Fragment() {
	
	companion object {
		private val arg_notebookSummary = "notebookSummary"
		fun newInstance(notebookSummary: NotebookShelf.NotebookSummary): NotebookExportFragment
			= NotebookExportFragment::class.java.newInstance().apply {
			arguments += bundleOf(arg_notebookSummary to notebookSummary)
		}
	}
	
}
