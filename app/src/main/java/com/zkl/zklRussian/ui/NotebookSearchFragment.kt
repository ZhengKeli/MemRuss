package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.Note

class NotebookSearchFragment : NotebookHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= NotebookSearchFragment::class.java.newInstance(notebookKey)
	}
	
	lateinit var sv_search: SearchView
	lateinit var tv_foundNothing: TextView
	lateinit var pb_searching: ProgressBar
	lateinit var lv_notes: ListView
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook_search, container, false).apply {
		sv_search = findViewById(R.id.sv_search)
		tv_foundNothing = findViewById(R.id.tv_foundNothing)
		pb_searching = findViewById(R.id.pb_searching)
		lv_notes = findViewById(R.id.lv_notes)
	}.apply {
		
		sv_search.isIconified = false
		sv_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				searchText(query)
				return true
			}
			
			override fun onQueryTextChange(newText: String): Boolean {
				searchText(newText)
				return true
			}
			
			fun searchText(text: String) {
				searcher.post(text.trim())
			}
		})
		
		lv_notes.adapter = object : NoteListAdapter() {
			override fun getCount() = searchResult.size
			override fun getItem(position: Int) = searchResult[position]
			override val context: Context get() = activity
		}
		lv_notes.setOnItemClickListener { _, _, position, _ ->
			val note = searchResult[position]
			val fragment = NoteViewFragment.newInstance(notebookKey, note.id)
			fragment.jump(fragmentManager, true)
		}
		
	}
	
	//search
	val searchResult: ArrayList<Note> = arrayListOf()
	val searcher = object : PendingWorker<String, List<Note>>() {
		override fun onWork(request: String): List<Note> {
			val requestEmpty = request.isEmpty()
			tv_foundNothing.post {
				if (requestEmpty) {
					tv_foundNothing.visibility = View.VISIBLE
					tv_foundNothing.text = getString(R.string.type_to_search)
				} else {
					tv_foundNothing.visibility = View.GONE
				}
			}
			
			var isDone = false
			pb_searching.postDelayed({
				pb_searching.visibility = if (!isDone) View.VISIBLE else View.GONE
			}, 500)
			
			val result =
				if (request.isEmpty()) emptyList()
				else {
					Thread.sleep(100) //延迟一下避免资源过度消耗
					notebook.selectByKeyword(request)
				}
			
			isDone = true
			
			return result
		}
		
		override fun onDone(request: String, result: List<Note>) {}
		override fun onAllDone(lastRequest: String, lastResult: List<Note>) {
			val requestEmpty = lastRequest.isEmpty()
			val resultEmpty = lastResult.isEmpty()
			lv_notes.post {
				pb_searching.visibility = View.GONE
				if (resultEmpty) {
					tv_foundNothing.visibility = View.VISIBLE
					tv_foundNothing.text =
						if (requestEmpty) getString(R.string.type_to_search)
						else getString(R.string.found_nothing)
				} else tv_foundNothing.visibility = View.GONE
				
				searchResult.clear()
				searchResult.addAll(lastResult)
				(lv_notes.adapter as BaseAdapter).notifyDataSetChanged()
			}
		}
	}
	
}


