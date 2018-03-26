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
				search(newText.trim())
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
	private var searchJob: Job? = null
	private var searchResult: List<Note> = emptyList()
	private fun search(searchText: String) {
		val oldJob = searchJob
		searchJob = launch(CommonPool) {
			oldJob?.cancelAndJoin()
			
			val searchingUI = launch(coroutineContext + UI) ui@{
				if (!isVisible) return@ui
				tv_foundNothing.visibility = View.GONE
				lv_notes.visibility = View.GONE
				
				delay(500)
				
				if (!isVisible) return@ui
				pb_searching.visibility = View.VISIBLE
			}
			
			val result =
				if (searchText.isEmpty()) emptyList()
				else async(coroutineContext + CommonPool) {
					delay(200)
					notebook.selectByKeyword(searchText)
				}.await()
			
			searchingUI.cancel()
			launch(coroutineContext + UI) ui@{
				if (!isVisible) return@ui
				
				tv_foundNothing.run {
					if (result.isEmpty()) {
						visibility = View.VISIBLE
						text =
							if (!searchText.isEmpty()) getString(R.string.found_nothing)
							else getString(R.string.type_to_search)
					} else {
						visibility = View.GONE
					}
				}
				pb_searching.visibility = View.GONE
				
				searchResult = result
				(lv_notes.adapter as BaseAdapter).notifyDataSetChanged()
				lv_notes.visibility = View.VISIBLE
			}
			
		}
	}
	
}


