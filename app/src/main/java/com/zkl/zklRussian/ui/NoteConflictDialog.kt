package com.zkl.zklRussian.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.ConflictSolution
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.base.isLearning
import kotlinx.android.synthetic.main.dialog_note_conflict.view.*
import org.jetbrains.anko.bundleOf
import java.io.Serializable

class NoteConflictDialog : NotebookHoldingDialog() {
	
	data class ConflictSituation(
		val isAdding: Boolean, val conflictNoteId: Long,
		val newContent: NoteContent, val hasNewMemoryState: Boolean) : Serializable
	
	interface ConflictSolvedListener {
		fun onConflictSolved(solution: ConflictSolution?)
	}
	
	companion object {
		private val arg_conflictSituation = "conflictSituation"
		private val arg_cancelable = "cancelable"
		fun <T> newInstance(notebookKey: NotebookKey, situation: ConflictSituation, cancelable: Boolean, solvedListener: T)
			where T : ConflictSolvedListener, T : Fragment
			= NoteConflictDialog::class.java.newInstance(notebookKey).apply {
			arguments += bundleOf(
				arg_conflictSituation to situation,
				arg_cancelable to cancelable
			)
			setTargetFragment(solvedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
		
		//prepare data
		val situation = arguments.getSerializable(arg_conflictSituation) as ConflictSituation
		val cancelable = arguments.getBoolean(arg_cancelable, false)
		val conflictNote = notebook.getNote(situation.conflictNoteId)
		
		//prepare views
		val view = View.inflate(context, R.layout.dialog_note_conflict, null)
		val dialogBuilder = AlertDialog.Builder(context).setView(view)
		
		dialogBuilder.setTitle(R.string.there_are_conflicted_notes)
		
		view.tv_newContent.setText(if (situation.isAdding) R.string.adding_note else R.string.modifying_note)
		view.fl_newContent.addView(situation.newContent.newItemHolderOrThrow(context,view.fl_newContent).view)
		view.fl_conflictContent.addView(conflictNote.content.newItemHolderOrThrow(context,view.fl_newContent).view)
		
		if (conflictNote.isLearning) {
			view.cb_coverProgress.visibility = View.VISIBLE
			view.cb_coverProgress.isChecked = !situation.isAdding
			if (situation.hasNewMemoryState)
				view.cb_coverProgress.setText(R.string.cover_memory_progress)
		} else {
			view.cb_coverProgress.visibility = View.GONE
			view.cb_coverProgress.isChecked = situation.hasNewMemoryState
		}
		
		dialogBuilder.setPositiveButton(R.string.override) { _, _ ->
			makeCallback(true, view.cb_coverProgress.isChecked)
		}
		if (cancelable) dialogBuilder.setNegativeButton(R.string.cancel) { _, _ ->
			makeNullCallback()
		}
		else dialogBuilder.setNegativeButton(R.string.remain_old) { _, _ ->
			makeCallback(false, false)
		}
		
		isCancelable = cancelable
		return dialogBuilder.create()
	}
	
	override fun onCancel(dialog: DialogInterface?) {
		super.onCancel(dialog)
		makeNullCallback()
	}
	
	private fun makeCallback(override: Boolean, resetProgress: Boolean) {
		(targetFragment as? ConflictSolvedListener)
			?.onConflictSolved(ConflictSolution(override, resetProgress))
	}
	
	private fun makeNullCallback() {
		(targetFragment as? ConflictSolvedListener)
			?.onConflictSolved(null)
	}
	
}

