package com.zkl.memruss.ui

import android.annotation.SuppressLint
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
import com.zkl.memruss.core.note.InstantNote
import com.zkl.memruss.core.note.MutableNotebook
import com.zkl.memruss.core.note.base.NoteMemoryState
import com.zkl.memruss.core.note.base.NotebookMemoryStatus
import com.zkl.memruss.core.note.base.isLearning
import com.zkl.memruss.core.note.rawAddNote
import kotlinx.android.synthetic.main.dialog_notebook_merging.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.bundleOf
import java.io.Serializable
import java.util.concurrent.ArrayBlockingQueue

class NotebookMergingDialog : DialogFragment(),
	NoteConflictDialog.ConflictSolvedListener {
	
	data class MergeRequest(val mainBodyKey: NotebookKey, val attachmentKey: NotebookKey,
	                        val keepProgress: Boolean, val deleteOld: Boolean) : Serializable
	
	interface MergeCompletedListener{
		fun onMergeCompleted(notebookKey: NotebookKey)
	}
	
	companion object {
		private val arg_mergeRequest = "mergeRequest"
		fun <T> newInstance(mergeRequest: MergeRequest, mergedListener: T): NotebookMergingDialog
			where T : MergeCompletedListener, T : Fragment
			= NotebookMergingDialog::class.java.newInstance().apply {
			arguments += bundleOf(arg_mergeRequest to mergeRequest)
			setTargetFragment(mergedListener, 0)
		}
		
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
		val view = View.inflate(activity, R.layout.dialog_notebook_merging, null)
		doMerging { progress, max ->
			launch(UI) {
				@SuppressLint("SetTextI18n")
				view.tv_merge_progress.text = "$progress/$max"
				view.pb_merge_progress.max = max
				view.pb_merge_progress.progress = progress
			}
		}
		isCancelable = false
		return AlertDialog.Builder(activity)
			.setTitle(getString(R.string.merging_notebooks))
			.setView(view)
			.create()
	}
	
	private fun doMerging(onProgress: ((progress: Int, max: Int) -> Unit)) {
		launch(CommonPool) {
			val mergeRequest = arguments[arg_mergeRequest] as MergeRequest
			val mainBody = myApp.notebookShelf.restoreNotebook(mergeRequest.mainBodyKey) as MutableNotebook
			val attachment = myApp.notebookShelf.restoreNotebook(mergeRequest.attachmentKey)
			val notesToAdd = attachment.rawGetAllNotes()
			
			//notes
			onProgress(0, notesToAdd.size)
			notesToAdd.forEachIndexed { index, note ->
				val filteredNote =
					if (mergeRequest.keepProgress) note
					else InstantNote(note,
						memoryState = NoteMemoryState.infantState(),
						memoryUpdateTime = System.currentTimeMillis())
				mainBody.rawAddNote(filteredNote) { conflictNoteId, newNote ->
					val situation = NoteConflictDialog.ConflictSituation(
						true, conflictNoteId,
						newNote.content, newNote.isLearning)
					launch(UI) {
						NoteConflictDialog.newInstance(mergeRequest.mainBodyKey, situation, false,
							this@NotebookMergingDialog).show(fragmentManager)
					}
					conflictSolutionChan.take()?: ConflictSolution(false, false)
				}
				onProgress(index + 1, notesToAdd.size)
			}
			
			//plan
			if (mergeRequest.keepProgress && attachment.memoryState.status != NotebookMemoryStatus.INFANT) {
				mainBody.memoryPlan = attachment.memoryPlan
				mainBody.memoryState = attachment.memoryState
			}
			
			//delete
			if (mergeRequest.deleteOld) {
				attachment.close()
				myApp.notebookShelf.deleteNotebook(mergeRequest.attachmentKey)
			}
			
			dismiss()
		}
	}
	
	private val conflictSolutionChan = ArrayBlockingQueue<ConflictSolution?>(1)
	override fun onConflictSolved(solution: ConflictSolution?) {
		conflictSolutionChan.put(solution)
	}
	
	override fun onDismiss(dialog: DialogInterface?) {
		super.onDismiss(dialog)
		(targetFragment as? MergeCompletedListener)?.onMergeCompleted((arguments[arg_mergeRequest] as MergeRequest).mainBodyKey)
	}
	
}