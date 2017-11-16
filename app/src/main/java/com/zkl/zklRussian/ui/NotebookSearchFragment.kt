package com.zkl.zklRussian.ui

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
	
	private lateinit var sv_search:SearchView
	private lateinit var tv_nothing:TextView
	private lateinit var tv_typeToSearch:TextView
	private lateinit var pb_searching:ProgressBar
	private lateinit var lv_notes:ListView
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook_search, container, false).apply {
		
		sv_search = findViewById(R.id.sv_search) as SearchView
		tv_nothing = findViewById(R.id.tv_nothing) as TextView
		tv_typeToSearch = findViewById(R.id.tv_typeToSearch) as TextView
		pb_searching = findViewById(R.id.pb_searching) as ProgressBar
		lv_notes = findViewById(R.id.lv_notes) as ListView
		
	}
	override fun onStart() {
		super.onStart()
		
		sv_search.isIconified = false
		sv_search.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
				override fun onQueryTextSubmit(query: String): Boolean {
					searchText(query)
					return true
				}
				override fun onQueryTextChange(newText: String): Boolean {
					searchText(newText)
					return true
				}
				fun searchText(text:String){
					searcher.post(text)
				}
			})
		
		lv_notes.adapter = object : BaseAdapter() {
			override fun getItem(position: Int) = searchResult[getItemId(position).toInt()]
			override fun getItemId(position: Int) = position.toLong()
			
			override fun getCount() = searchResult.size
			override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
				val view = (convertView as? NoteItemView) ?: NoteItemView(activity)
				view.note = getItem(position)
				return view
			}
		}
		lv_notes.setOnItemClickListener { _, view, _, _ ->
			view as NoteItemView
			val fragment = NoteViewFragment.newInstance(notebookKey, view.note!!.id)
			fragmentManager.jumpTo(fragment,true)
		}
		
	}
	
	private fun updateNoteList(){
		(lv_notes.adapter as BaseAdapter).notifyDataSetChanged()
	}
	
	
	//search
	private var searchResult: ArrayList<Note> = arrayListOf()
	private val searcher = object :PendingWorker<String, List<Note>>(){
		override fun onWork(request: String): List<Note> {
			val requestEmpty = request.isEmpty()
			tv_typeToSearch.post {
				tv_typeToSearch.visibility = if (requestEmpty) View.VISIBLE else View.GONE
			}
			
			var isDone = false
			pb_searching.postDelayed({
				
				
				pb_searching.visibility = if (!isDone) View.VISIBLE else View.GONE
			},500)
			
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
			pb_searching.post {
				pb_searching.visibility = View.GONE
				if (requestEmpty) {
					tv_typeToSearch.visibility = View.VISIBLE
					tv_nothing.visibility = View.GONE
				}else{
					tv_typeToSearch.visibility = View.GONE
					tv_nothing.visibility = if (resultEmpty) View.VISIBLE else View.GONE
				}
			}
			lv_notes.post {
				searchResult.clear()
				searchResult.addAll(lastResult)
				updateNoteList()
			}
		}
	}
	
}


