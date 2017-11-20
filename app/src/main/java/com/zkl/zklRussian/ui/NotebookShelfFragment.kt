package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookShelf
import org.jetbrains.anko.bundleOf

class NotebookShelfFragment:Fragment(){
	
	companion object {
		private val arg_autoJump: String = "autoJump"
		fun newInstance(autoJumpToFirst: Boolean)
			= NotebookShelfFragment().apply {
			arguments = bundleOf(
				arg_autoJump to autoJumpToFirst
			)
		}
		private var NotebookShelfFragment.autoJump:Boolean
			get() = arguments!!.getBoolean(arg_autoJump)
			set(value) {
				arguments!!.putBoolean(arg_autoJump,value)
			}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook_shelf,container,false).apply {
		
		val b_create = findViewById(R.id.b_create) as Button
		val b_import = findViewById(R.id.b_import) as Button
		val b_export = findViewById(R.id.b_export) as Button
		val lv_notebooks = findViewById(R.id.lv_notebooks) as ListView
		
		val summaries = myApp.notebookShelf.loadBookSummaries()
		if (summaries.isEmpty()) fragmentManager.jumpTo(NotebookInfantFragment(),false)
		else if (autoJump) {
			val (key, _) = myApp.notebookShelf.openMutableNotebook(summaries.first().file)
			fragmentManager.jumpTo(NotebookFragment.newInstance(key), true)
			autoJump = false
		}
		
		b_create.setOnClickListener{
			fragmentManager.jumpTo(NotebookCreationFragment(),true)
		}
		b_import.setOnClickListener {
			TODO()
		}
		b_export.setOnClickListener{
			TODO()
		}
		
		lv_notebooks.adapter = object:BaseAdapter(){
			override fun getView(position: Int, convertView: View?, parent: ViewGroup?): NotebookItemView
				= ((convertView as? NotebookItemView) ?: NotebookItemView(context)).apply { notebookSummary=getItem(position) }
			
			override fun getCount() = summaries.size
			
			override fun getItem(position: Int) = summaries[position]
			
			override fun getItemId(position: Int) = 0L
			
		}
		
		lv_notebooks.setOnItemClickListener { parent, _, position, _ ->
			val summary = parent.adapter.getItem(position) as NotebookShelf.NotebookSummary
			val (key, _) = myApp.notebookShelf.openMutableNotebook(summary.file)
			fragmentManager.jumpTo(NotebookFragment.newInstance(key),true)
		}
	}
	
	
}


class NotebookItemView(context: Context):LinearLayout(context){
	private val tv_notebookName:TextView
	init {
		LayoutInflater.from(context).inflate(R.layout.adapter_notebook_item, this, true)
		tv_notebookName= findViewById(R.id.tv_notebookName) as TextView
	}
	
	var notebookSummary: NotebookShelf.NotebookSummary? = null
		set(value) {
			field = value
			tv_notebookName.text = value?.bookName ?: ""
		}
}

