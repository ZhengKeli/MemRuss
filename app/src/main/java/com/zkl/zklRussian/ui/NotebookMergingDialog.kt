package com.zkl.zklRussian.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.View
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookBrief
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.ConflictSolution
import com.zkl.zklRussian.core.note.InstantNote
import com.zkl.zklRussian.core.note.base.NoteMemoryState
import com.zkl.zklRussian.core.note.base.isLearning
import com.zkl.zklRussian.core.note.rawAddNote
import kotlinx.android.synthetic.main.dialog_notebook_merging.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.bundleOf
import java.io.Serializable
import java.util.concurrent.ArrayBlockingQueue

class NotebookMergingDialog : DialogFragment(),
	NoteConflictDialog.ConflictSolvedListener {
	
	data class MergeRequest(val mainBody: NotebookBrief, val attachment: NotebookBrief,
	                        val keepPlan: Boolean, val deleteOld: Boolean) : Serializable
	
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
			val (key, mainBody) = myApp.notebookShelf.openMutableNotebook(mergeRequest.mainBody.file)
			val (_, attachment) = myApp.notebookShelf.openReadOnlyNotebook(mergeRequest.attachment.file)
			val notesToAdd = attachment.rawGetAllNotes()
			val resetMemoryState = !mergeRequest.keepPlan
			
			onProgress(0, notesToAdd.size)
			notesToAdd.forEachIndexed { index, note ->
				val filteredNote =
					if (!resetMemoryState) note
					else InstantNote(note,
						memoryState = NoteMemoryState.infantState(),
						memoryUpdateTime = System.currentTimeMillis())
				mainBody.rawAddNote(filteredNote) { conflictNoteId, newNote ->
					val situation = NoteConflictDialog.ConflictSituation(
						true, conflictNoteId,
						newNote.content, newNote.isLearning)
					launch(UI){
						NoteConflictDialog.newInstance(key, situation, false,
							this@NotebookMergingDialog).show(fragmentManager)
					}
					conflictSolutionChan.take()
				}
				onProgress(index + 1, notesToAdd.size)
			}
			
			if (mergeRequest.deleteOld) {
				attachment.close()
				myApp.notebookShelf.deleteNotebook(mergeRequest.attachment.file)
			}
			
			dismiss()
			(targetFragment as? MergeCompletedListener)?.onMergeCompleted(key)
			
		}
	}
	
	private val conflictSolutionChan = ArrayBlockingQueue<ConflictSolution>(1)
	override fun onConflictSolved(solution: ConflictSolution?) {
		conflictSolutionChan.put(solution ?: ConflictSolution(false, false))
	}
	
}