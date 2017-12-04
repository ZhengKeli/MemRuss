package com.zkl.zklRussian.ui

import android.support.v4.app.Fragment
import com.zkl.zklRussian.control.note.NotebookBrief
import org.jetbrains.anko.bundleOf

class NotebookExportFragment : Fragment() {
	
	companion object {
		private val arg_notebookBrief = "notebookBrief"
		fun newInstance(notebookBrief: NotebookBrief): NotebookExportFragment
			= NotebookExportFragment::class.java.newInstance().apply {
			arguments += bundleOf(arg_notebookBrief to notebookBrief)
		}
	}
	
}
