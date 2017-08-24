package com.zkl.ZKLRussian.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.zkl.ZKLRussian.R
import com.zkl.ZKLRussian.core.note.NoteContent
import com.zkl.ZKLRussian.core.note.QuestionContent


enum class ViewMode { view, edit, review }
data class NoteContentViewSource(
	val noteContent: NoteContent,
	val viewMode: ViewMode = ViewMode.view
)


interface NoteContentViewBuilder{
	fun buildView(source: NoteContentViewSource,context: Context,parent:ViewGroup?=null):View
	fun updateView(source: NoteContentViewSource,view:View?=null):Boolean
}
val noteContentViewBuilders = hashMapOf<String,NoteContentViewBuilder>(
	QuestionContent::class.simpleName!! to object : NoteContentViewBuilder {
		override fun buildView(source: NoteContentViewSource, context: Context, parent: ViewGroup?): View {
			
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}
		override fun updateView(source: NoteContentViewSource, view: View?): Boolean {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}
		
	}
)

class QuestionContentView(context: Context):LinearLayout(context){
	
	private val tv_question:TextView
	private val tv_answer:TextView
	private val b_show: Button
	private val ll_result_buttons:LinearLayout
	private val b_pass:Button
	private val b_failed:Button
	init {
		View.inflate(context, R.layout.cv_question_content,this)
		tv_question = findViewById(R.id.tv_word) as TextView
		tv_answer = findViewById(R.id.tv_meaning) as TextView
		b_show = findViewById(R.id.b_show) as Button
		ll_result_buttons = findViewById(R.id.ll_result_buttons) as LinearLayout
		b_pass = findViewById(R.id.b_pass) as Button
		b_failed= findViewById(R.id.b_failed) as Button
		
		b_show.setOnClickListener{
			TODO()
		}
	}
	
	fun setContent(questionContent: QuestionContent) {
		tv_question.text=questionContent.question
		tv_answer.text=questionContent.answer
	}
	
	var viewMode:ViewMode = ViewMode.view
	fun setMode(viewMode: ViewMode){
		when(viewMode){
			ViewMode.view -> TODO()
			ViewMode.edit -> TODO()
			ViewMode.review -> TODO()
		}
	}
	
	
}

