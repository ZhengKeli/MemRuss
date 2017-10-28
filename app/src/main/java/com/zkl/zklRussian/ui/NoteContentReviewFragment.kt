package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.core.note.NoteContent
import com.zkl.zklRussian.core.note.NoteMemory
import com.zkl.zklRussian.core.note.QuestionContent
import com.zkl.zklRussian.core.note.getNextReviewTime


abstract class NoteContentReviewFragment: Fragment(){
	abstract var noteContent: NoteContent?
	abstract fun isCompatible(noteContent: NoteContent):Boolean
	abstract var onResultListener:((ReviewResult)->Unit)?
}

interface ReviewResult{
	fun updateNoteMemory(oldMemory:NoteMemory,nowTime:Long=System.currentTimeMillis()):NoteMemory
}
val standardIncreaseResult = object : ReviewResult {
	override fun updateNoteMemory(oldMemory: NoteMemory, nowTime: Long)
		= oldMemory.getNextReviewTime(true, nowTime)
}
val standardDegreaseResult = object : ReviewResult {
	override fun updateNoteMemory(oldMemory: NoteMemory, nowTime: Long)
		= oldMemory.getNextReviewTime(false, nowTime)
}

val typedNoteContentReviewFragments = hashMapOf<String,Class<out NoteContentReviewFragment>>(
	QuestionContent::class.simpleName!! to QuestionContentReviewFragment::class.java
)

class QuestionContentReviewFragment:NoteContentReviewFragment(){
	
	//views
	private var tv_question: TextView? = null
	private var tv_answer: TextView? = null
	private var b_show: Button? = null
	private var ll_resultBar: LinearLayout? = null
	private lateinit var b_remembered: Button
	private lateinit var b_forgot: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= inflater.inflate(R.layout.fragment_note_content_review_question,container,false).apply {
		tv_question = findViewById(R.id.tv_question) as TextView
		tv_answer = findViewById(R.id.tv_answer) as TextView
		b_show = findViewById(R.id.b_show) as Button
		ll_resultBar = findViewById(R.id.ll_result_bar) as LinearLayout
		b_remembered = findViewById(R.id.b_remembered) as Button
		b_forgot = findViewById(R.id.b_forgot) as Button
	}
	override fun onDestroyView() {
		super.onDestroyView()
		tv_question = null
		tv_answer = null
		b_show = null
		ll_resultBar = null
	}
	override fun onStart() {
		super.onStart()
		updateViews()
		
		b_show?.setOnClickListener{
			b_show?.visibility = View.GONE
			tv_answer?.visibility = View.VISIBLE
			ll_resultBar?.visibility = View.VISIBLE
		}
		
		b_remembered.setOnClickListener {
			onResultListener?.invoke(standardIncreaseResult)
		}
		
		b_forgot.setOnClickListener {
			onResultListener?.invoke(standardDegreaseResult)
		}
	}
	
	private fun updateViews() {
		tv_question?.text = questionContent?.question ?: ""
		tv_answer?.text = questionContent?.answer ?: ""
		
		b_show?.visibility = View.VISIBLE
		tv_answer?.visibility = View.GONE
		ll_resultBar?.visibility = View.INVISIBLE
	}
	
	private var questionContent: QuestionContent? = null
	set(value) {
		field = value
		updateViews()
	}
	override var noteContent: NoteContent?
		get() = questionContent
		set(value) {
			questionContent = value as QuestionContent
		}
	
	override fun isCompatible(noteContent: NoteContent)
		= noteContent is QuestionContent
	
	override var onResultListener: ((reviewResult:ReviewResult) -> Unit)? = null
	
}


