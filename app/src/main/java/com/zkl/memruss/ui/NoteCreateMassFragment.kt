package com.zkl.memruss.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.memruss.R
import com.zkl.memruss.control.note.NoteContentTextParser
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.ConflictSolution
import com.zkl.memruss.core.note.addNotes
import kotlinx.android.synthetic.main.fragment_note_create_mass.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.ArrayBlockingQueue

class NoteCreateMassFragment : NotebookHoldingFragment(),
	NoteConflictDialog.ConflictSolvedListener,
	NoteCreateFromFileDialog.NotesFileSelectedListener{
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= NoteCreateMassFragment::class.java.newInstance(notebookKey)
	}
	
	//view
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_create_mass, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		b_fromFile.setOnClickListener {
			NoteCreateFromFileDialog.newInstance(this).show(fragmentManager)
		}
		
		b_ok.setOnClickListener {
			val text = et_contents.text.toString()
			launch(CommonPool){
				val contents = NoteContentTextParser.parse(text)
				if (contents.isEmpty()){
					launch(UI){ toast(R.string.can_not_recognize_any_note) }
					return@launch
				}
				mutableNotebook.addNotes(contents) { conflictNoteId, newContent ->
					val situation = NoteConflictDialog.ConflictSituation(
						true, conflictNoteId, newContent, false)
					launch(UI) {
						NoteConflictDialog.newInstance(notebookKey, situation,
							false, this@NoteCreateMassFragment).show(fragmentManager)
					}
					conflictSolutionChan.take() ?: ConflictSolution(false, false)
				}
				launch(UI) { fragmentManager.popBackStack() }
			}
		}
		b_cancel.setOnClickListener {
			fragmentManager.popBackStack()
		}
	}
	
	override fun onNotesFileSelected(file: File, charset: Charset) {
		mainActivity.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,true) { granted, _ ->
			if (!granted) {
				mainActivity.toast(R.string.no_sdcard_read_permission)
				return@requestPermission
			}
			if (!file.exists()) {
				toast(R.string.file_not_exists)
				return@requestPermission
			}
			launch(CommonPool){
				val string = file.readText(charset).trimStart('\uFEFF') //utf8编码可能带BOM头，要去掉
				launch(UI){ et_contents.setText(string) }
			}
		}
		
	}
	
	private val conflictSolutionChan = ArrayBlockingQueue<ConflictSolution?>(1)
	override fun onConflictSolved(solution: ConflictSolution?) {
		conflictSolutionChan.put(solution)
	}
	
}

