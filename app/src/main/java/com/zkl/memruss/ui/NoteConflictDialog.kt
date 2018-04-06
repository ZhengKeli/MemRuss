package com.zkl.memruss.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.View
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.ConflictSolution
import com.zkl.memruss.core.note.NoteContent
import com.zkl.memruss.core.note.base.isLearning
import kotlinx.android.synthetic.main.dialog_note_conflict.view.*
import org.jetbrains.anko.bundleOf
import java.io.Serializable

class NoteConflictDialog : DialogFragment() {
	
	data class ConflictSituation(
		val isAdding: Boolean,
		val conflictNoteId: Long,
		val newContent: NoteContent,
		val hasNewMemoryState: Boolean
	) : Serializable
	
	data class DialogResult(
		val canceled: Boolean,
		val solution: ConflictSolution?
	)
	
	interface DialogResultedListener {
		fun onDialogResulted(result: DialogResult)
	}
	
	companion object {
		private const val argName_ConflictSituation = "conflictSituation"
		private const val argName_cancelable = "cancelable"
		fun <T> newInstance(notebookKey: NotebookKey, situation: ConflictSituation, cancelable: Boolean, solvedListener: T)
			where T : DialogResultedListener, T : Fragment = NoteConflictDialog::class.java.newInstance(notebookKey).apply {
			arguments += bundleOf(
				argName_ConflictSituation to situation,
				argName_cancelable to cancelable
			)
			setTargetFragment(solvedListener, 0)
		}
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
		
		//prepare data
		val notebook = myApp.notebookShelf.restoreNotebook(argNotebookKey)
		val situation = arguments.getSerializable(argName_ConflictSituation) as ConflictSituation
		val cancelable = arguments.getBoolean(argName_cancelable, false)
		val conflictNote = notebook.getNote(situation.conflictNoteId)
		
		//prepare views
		val view = View.inflate(context, R.layout.dialog_note_conflict, null)
		val dialogBuilder = AlertDialog.Builder(context).setView(view)
		
		dialogBuilder.setTitle(R.string.there_are_conflicted_notes)
		
		view.tv_newContent.setText(if (situation.isAdding) R.string.adding_note else R.string.modifying_note)
		view.fl_newContent.addView(situation.newContent.newItemHolderOrThrow(context, view.fl_newContent).view)
		view.fl_conflictContent.addView(conflictNote.content.newItemHolderOrThrow(context, view.fl_newContent).view)
		
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
		(targetFragment as? DialogResultedListener)
			?.onDialogResulted(DialogResult(false, ConflictSolution(override, resetProgress)))
	}
	
	private fun makeNullCallback() {
		(targetFragment as? DialogResultedListener)
			?.onDialogResulted(DialogResult(true, null))
	}
	
}

