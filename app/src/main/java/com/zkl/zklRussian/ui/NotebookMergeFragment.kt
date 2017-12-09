package com.zkl.zklRussian.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ScrollView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp
import com.zkl.zklRussian.control.note.NotebookBrief
import kotlinx.android.synthetic.main.fragment_notebook_merge.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.support.v4.toast

class NotebookMergeFragment : Fragment(){
	
	companion object {
		val arg_brief1 = "brief1"
		val arg_brief2 = "brief2"
		fun newInstance(brief1: NotebookBrief? = null, brief2: NotebookBrief? = null): NotebookMergeFragment
			= NotebookMergeFragment::class.java.newInstance().apply {
			arguments += bundleOf(arg_brief1 to brief1, arg_brief2 to brief2)
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook_merge, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		briefs1 = myApp.notebookShelf.loadNotebookBriefs()
		
		sp_notebook1.adapter = object : NotebookListAdapter() {
			override fun getCount() = briefs1.size
			override fun getItem(position: Int) = briefs1[position]
			override val context: Context get() = activity
		}
		sp_notebook2.adapter = object : NotebookListAdapter() {
			override fun getCount() = briefs2.size
			override fun getItem(position: Int) = briefs2[position]
			override val context: Context get() = activity
		}
		
		sp_notebook1.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
			override fun onNothingSelected(parent: AdapterView<*>?) {
				brief1 = null
				cb_keep_plan1.visibility = View.GONE
				cb_delete_after_merge1.visibility = View.GONE
				ll_notebook2.visibility = View.GONE
			}
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				val brief1 = (sp_notebook1.adapter as NotebookListAdapter).getItem(position)
				this@NotebookMergeFragment.brief1 = brief1
				
				cb_keep_plan1.visibility = if (brief1.hasPlan) View.VISIBLE else View.GONE
				cb_delete_after_merge1.visibility = View.VISIBLE
				
				briefs2 = briefs1.filter { it != brief1 }
				(sp_notebook2.adapter as BaseAdapter).notifyDataSetChanged()
				ll_notebook2.visibility = View.VISIBLE
				sv_content.fullScroll(ScrollView.FOCUS_DOWN)
			}
		}
		sp_notebook2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onNothingSelected(parent: AdapterView<*>?) {
				brief2 = null
				cb_keep_plan2.visibility = View.GONE
				cb_delete_after_merge2.visibility = View.GONE
				b_merge.visibility = View.GONE
			}
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				val brief2 = (sp_notebook2.adapter as NotebookListAdapter).getItem(position)
				this@NotebookMergeFragment.brief2 = brief2
				
				cb_keep_plan2.visibility = if (brief2.hasPlan) View.VISIBLE else View.GONE
				cb_delete_after_merge2.visibility = View.VISIBLE
				
				b_merge.visibility = View.VISIBLE
				sv_content.fullScroll(ScrollView.FOCUS_DOWN)
			}
		}
		
		b_merge.setOnClickListener {
			//todo do merge
			toast("假装在合并单词本")
			fragmentManager.popBackStack()
		}
		
		brief1 = arguments.getSerializable(arg_brief1) as? NotebookBrief
		brief2 = arguments.getSerializable(arg_brief2) as? NotebookBrief
		briefs1.indexOf(brief1).let { if (it != -1) sp_notebook1.setSelection(it) }
		briefs2.indexOf(brief2).let { if (it != -1) sp_notebook2.setSelection(it) }
		
	}
	
	var briefs1: List<NotebookBrief> = emptyList()
	var briefs2: List<NotebookBrief> = emptyList()
	var brief1: NotebookBrief? = null
		set(value) {
			field = value
			arguments.putSerializable(arg_brief1, value)
		}
	var brief2: NotebookBrief? = null
		set(value) {
			field = value
			arguments.putSerializable(arg_brief2, value)
		}
	
}