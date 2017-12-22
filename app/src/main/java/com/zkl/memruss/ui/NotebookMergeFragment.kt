package com.zkl.memruss.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ScrollView
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookBrief
import com.zkl.memruss.control.note.NotebookKey
import kotlinx.android.synthetic.main.fragment_notebook_merge.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.support.v4.toast

class NotebookMergeFragment : Fragment(),
	NotebookMergingDialog.MergeCompletedListener {
	
	interface NotebookMergedListener {
		fun onNotebookMerged(notebookKey: NotebookKey)
	}
	
	companion object {
		val arg_mainBody = "mainBody"
		val arg_attachment = "attachment"
		fun <T> newInstance(mainBody: NotebookBrief?, attachment: NotebookBrief?, mergedListener: T?): NotebookMergeFragment
			where T : NotebookMergedListener, T : Fragment
			= NotebookMergeFragment::class.java.newInstance().apply {
			arguments += bundleOf(arg_mainBody to mainBody, arg_attachment to attachment)
			setTargetFragment(mergedListener, 0)
		}
		fun <T> newInstance(mainBody: NotebookBrief, mergedListener: T?): NotebookMergeFragment
			where T : NotebookMergedListener, T : Fragment
			= newInstance(mainBody, null, mergedListener)
		fun <T> newInstance(mergedListener: T?): NotebookMergeFragment
			where T : NotebookMergedListener, T : Fragment
			= newInstance(null, null, mergedListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_notebook_merge, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		allNotebooks = myApp.notebookShelf.loadNotebookBriefs()
		mainBodies = allNotebooks.filter { it.mutable }
		
		sp_mainBody.adapter = object : NotebookListAdapter() {
			override fun getCount() = mainBodies.size
			override fun getItem(position: Int) = mainBodies[position]
			override val context: Context get() = activity
		}
		sp_attachment.adapter = object : NotebookListAdapter() {
			override fun getCount() = attachments.size
			override fun getItem(position: Int) = attachments[position]
			override val context: Context get() = activity
		}
		
		sp_mainBody.onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
			override fun onNothingSelected(parent: AdapterView<*>?) {
				mainBody = null
				b_merge.isEnabled = false
			}
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				val brief = (sp_mainBody.adapter as NotebookListAdapter).getItem(position)
				this@NotebookMergeFragment.mainBody = brief
				
				attachments = allNotebooks.filter { it != brief }
				(sp_attachment.adapter as BaseAdapter).notifyDataSetChanged()
				sv_content.fullScroll(ScrollView.FOCUS_DOWN)
			}
		}
		sp_attachment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onNothingSelected(parent: AdapterView<*>?) {
				attachment = null
				cb_keep_progress.visibility = View.GONE
				cb_delete_old.visibility = View.GONE
				b_merge.isEnabled = false
			}
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				val brief = (sp_attachment.adapter as NotebookListAdapter).getItem(position)
				this@NotebookMergeFragment.attachment = brief
				
				if (mainBody?.hasPlan == true && brief.hasPlan){
					cb_keep_progress.visibility = View.VISIBLE
					cb_keep_progress.isChecked = true
				}else{
					cb_keep_progress.visibility =View.GONE
					cb_keep_progress.isChecked = false
				}
				cb_delete_old.visibility = View.VISIBLE
				cb_delete_old.isChecked = false
				
				b_merge.isEnabled = true
				sv_content.fullScroll(ScrollView.FOCUS_DOWN)
			}
		}
		
		//restore the pre-defined selections
		mainBody = arguments.getSerializable(arg_mainBody) as? NotebookBrief
		attachment = arguments.getSerializable(arg_attachment) as? NotebookBrief
		mainBodies.indexOf(mainBody).let { if (it != -1) sp_mainBody.setSelection(it) }
		attachments.indexOf(attachment).let { if (it != -1) sp_attachment.setSelection(it) }
		
		b_merge.setOnClickListener {
			b_merge.isEnabled = false
			val (mainBodyKey,_) = myApp.notebookShelf.openMutableNotebook(mainBody!!.file)
			val (attachmentKey,_) = myApp.notebookShelf.openReadOnlyNotebook(attachment!!.file)
			val request = NotebookMergingDialog.MergeRequest(
				mainBodyKey, attachmentKey, cb_keep_progress.isChecked, cb_delete_old.isChecked)
			NotebookMergingDialog.newInstance(request, this).show(fragmentManager)
		}
		
	}
	
	var allNotebooks: List<NotebookBrief> = emptyList()
	var mainBodies: List<NotebookBrief> = emptyList()
	var attachments: List<NotebookBrief> = emptyList()
	var mainBody: NotebookBrief? = null
		set(value) {
			field = value
			arguments.putSerializable(arg_mainBody, value)
		}
	var attachment: NotebookBrief? = null
		set(value) {
			field = value
			arguments.putSerializable(arg_attachment, value)
		}
	
	override fun onMergeCompleted(notebookKey: NotebookKey) {
		toast(R.string.merge_succeed)
		fragmentManager.popBackStack()
		(targetFragment as NotebookMergedListener).onNotebookMerged(notebookKey)
	}
	
}