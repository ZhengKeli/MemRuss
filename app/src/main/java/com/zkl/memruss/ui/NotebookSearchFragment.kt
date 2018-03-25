package com.zkl.memruss.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SearchView
import com.zkl.memruss.R
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.Note
import kotlinx.android.synthetic.main.fragment_notebook_search.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ConflatedChannel

class NotebookSearchFragment : NotebookHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey) = NotebookSearchFragment::class.java.newInstance(notebookKey)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.fragment_notebook_search, container, false)
	}
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		sv_search.isIconified = false
		sv_search.setOnCloseListener {
			fragmentManager.popBackStack()
			true
		}
		sv_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean = true
			override fun onQueryTextChange(newText: String): Boolean {
				startSearch(newText.trim())
				return true
			}
		})
		
		lv_notes.adapter = object : NoteListAdapter() {
			override fun getCount() = searchResult.size
			override fun getItem(position: Int): Note = searchResult[position]
			override val context: Context get() = activity
		}
		lv_notes.setOnItemClickListener { _, _, position, _ ->
			val note = searchResult[position]
			val fragment = NoteViewFragment.newInstance(notebookKey, note.id)
			fragment.jump(fragmentManager)
		}
	}
	
	
	//search
	data class Search(val text: String, val deferredResult: Deferred<List<Note>>)
	
	private val searchChan = ConflatedChannel<Search>().apply {
		launch(CommonPool) {
			while (isActive) {
				val (text, deferredResult) = receiveOrNull() ?: break
				val result = deferredResult.await()
				launch(UI) { resultStart(text, result) }
			}
		}
	}
	
	private var searchResult: List<Note> = emptyList()
	
	private fun startSearch(searchText: String) {
		val deferredResult = async(CommonPool, CoroutineStart.LAZY) {
			if (searchText.isEmpty()) return@async emptyList<Note>()
			delay(100)
			notebook.selectByKeyword(searchText)
		}
		launch(CommonPool) { searchChan.send(Search(searchText, deferredResult)) }
		launch(UI) {
			lv_notes.isEnabled = false
			delay(500)
			if (!this@NotebookSearchFragment.isVisible) return@launch
			if (deferredResult.isActive) pb_searching.visibility = View.VISIBLE
		}
	}
	
	private fun resultStart(searchText: String, result: List<Note>) {
		if (!this@NotebookSearchFragment.isVisible) return
		
		lv_notes.isEnabled = true
		pb_searching.visibility = View.GONE
		
		if (result.isEmpty()) {
			tv_foundNothing.visibility = View.VISIBLE
			tv_foundNothing.text =
				if (searchText.isEmpty()) getString(R.string.type_to_search)
				else getString(R.string.found_nothing)
		} else {
			tv_foundNothing.visibility = View.GONE
		}
		
		searchResult = result
		(lv_notes.adapter as BaseAdapter).notifyDataSetChanged()
	}
	
	
}


