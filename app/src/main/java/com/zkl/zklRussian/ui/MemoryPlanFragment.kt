package com.zkl.zklRussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.MemoryPlan
import com.zkl.zklRussian.core.note.NotebookMemoryStatus
import org.jetbrains.anko.find

class MemoryPlanFragment : NotebookHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= MemoryPlanFragment::class.java.newInstance(notebookKey)
	}
	
	private val memoryLoadRange = 0..500
	private var SeekBar.memoryLoad: Int
		get() = progress + memoryLoadRange.start
		set(value) {
			progress = value
		}
	
	lateinit var tv_title: TextView
	lateinit var tv_memoryLoad: TextView
	lateinit var sb_memoryLoad: SeekBar
	lateinit var b_cancel: Button
	lateinit var b_ok: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_memory_plan, container, false).apply {
		tv_title = find(R.id.tv_title)
		tv_memoryLoad = find(R.id.tv_memoryLoad)
		sb_memoryLoad = find(R.id.sb_memoryLoad)
		b_cancel = find(R.id.b_cancel)
		b_ok = find(R.id.b_ok)
	}.apply {
		sb_memoryLoad.max = memoryLoadRange.run { endInclusive - start }
		sb_memoryLoad.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
				tv_memoryLoad.text = getString(R.string.daily_review_times_value, seekBar.memoryLoad)
			}
			
			override fun onStartTrackingTouch(seekBar: SeekBar) {}
			override fun onStopTrackingTouch(seekBar: SeekBar) {}
		})
		b_cancel.setOnClickListener {
			fragmentManager.popBackStack()
		}
		b_ok.setOnClickListener {
			mutableNotebook.memoryPlan = MemoryPlan(
				targetLoad = sb_memoryLoad.memoryLoad.toDouble()
			)
			mutableNotebook.fillNotesByPlan()
			fragmentManager.popBackStack()
		}
		
		val memoryState = notebook.memoryState
		tv_title.text = when (memoryState.status) {
			NotebookMemoryStatus.infant -> getString(R.string.make_MemoryPlan)
			NotebookMemoryStatus.learning -> getString(R.string.MemoryPlan)
			NotebookMemoryStatus.paused -> getString(R.string.MemoryPlan_paused)
		}
		val memoryPlan = notebook.memoryPlan ?: MemoryPlan.default
		sb_memoryLoad.memoryLoad = Math.round(memoryPlan.targetLoad).toInt()
	}
	
	
}
