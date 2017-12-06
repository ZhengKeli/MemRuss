package com.zkl.zklRussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.MemoryPlan
import com.zkl.zklRussian.core.note.NotebookMemoryStatus
import kotlinx.android.synthetic.main.fragment_memory_plan.*

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
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_memory_plan, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
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
