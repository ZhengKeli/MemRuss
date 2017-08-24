package com.zkl.ZKLRussian.ui


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.zkl.ZKLRussian.R
import com.zkl.ZKLRussian.control.myApp
import com.zkl.ZKLRussian.core.note.DuplicateException
import com.zkl.ZKLRussian.core.note.MutableNotebook
import com.zkl.ZKLRussian.core.note.Notebook
import com.zkl.ZKLRussian.core.note.QuestionContent


class NoteCreationFragment() : Fragment() {
	
	//views
	private lateinit var et_question: EditText
	private lateinit var et_answer: EditText
	private lateinit var b_ok: Button
	private lateinit var b_cancel: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_note_creation, container, false).apply {
		
		et_question = findViewById(R.id.et_question) as EditText
		et_answer = findViewById(R.id.et_answer) as EditText
		b_ok = findViewById(R.id.b_ok) as Button
		b_cancel = findViewById(R.id.b_cancel) as Button
		
	}
	override fun onStart() {
		super.onStart()
		b_ok.setOnClickListener {
			val succeed = processCreation(
				question = et_question.text.toString(),
				answer = et_answer.text.toString())
			if (succeed)
				notebookActivity.jumpBackFragment()
		}
		b_cancel.setOnClickListener {
			notebookActivity.jumpBackFragment()
		}
	}
	
	
	//key
	private var notebookKey:Int = -1
	constructor(notebookKey:Int):this(){
		this.notebookKey = notebookKey
	}
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		notebookKey = savedInstanceState?.getInt(this::notebookKey.name) ?: notebookKey
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putInt(this::notebookKey.name, notebookKey)
	}
	
	
	//notebook
	private val _notebook: Notebook by lazy { myApp.noteManager.getRegisterNotebook(notebookKey)!! }
	private val mutableNotebook: MutableNotebook get() = _notebook as MutableNotebook
	private fun processCreation(question: String, answer: String): Boolean {
		return try {
			val noteContent = QuestionContent(question, answer)
			mutableNotebook.addNote(noteContent)
			true
		} catch (e: DuplicateException) {
			Toast.makeText(context,"该词条与已有词条（ID=${e.duplicatedNoteId}）有冲突！",Toast.LENGTH_SHORT).show()
			false
		}
	}
	
}