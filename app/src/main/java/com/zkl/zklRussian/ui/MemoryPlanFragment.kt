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

class MemoryPlanFragment : NotebookHoldingFragment() {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= MemoryPlanFragment::class.java.newInstance(notebookKey)
	}
	
	private lateinit var tv_title: TextView
	private lateinit var tv_memoryLoad: TextView
	private lateinit var sb_memoryLoad: SeekBar
	private lateinit var b_cancel: Button
	private lateinit var b_ok: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_memory_plan, container, false).apply {
		tv_title = findViewById(R.id.tv_title) as TextView
		tv_memoryLoad = findViewById(R.id.tv_memoryLoad) as TextView
		sb_memoryLoad = findViewById(R.id.sb_memoryLoad) as SeekBar
		b_cancel = findViewById(R.id.b_cancel) as Button
		b_ok = findViewById(R.id.b_ok) as Button
	}
	
	override fun onStart() {
		super.onStart()
		
		initViews()
		updateViews()
	}
	
	
	val memoryLoadRange = 0..500
	var SeekBar.memoryLoad: Int
		get() = progress + memoryLoadRange.start
		set(value) {
			progress = value
		}
	private fun initViews() {
		sb_memoryLoad.max = memoryLoadRange.run { endInclusive - start }
		sb_memoryLoad.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
				tv_memoryLoad.text = getString(R.string.memoryLoad_value, seekBar.memoryLoad)
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
	}
		
	
	private fun updateViews() {
		val memoryState = notebook.memoryState
		tv_title.text = when (memoryState.status) {
			NotebookMemoryStatus.infant -> getString(R.string.makeMemoryPlan)
			NotebookMemoryStatus.learning -> getString(R.string.MemoryPlan)
			NotebookMemoryStatus.paused -> getString(R.string.MemoryPlan_paused)
		}
		
		val memoryPlan = notebook.memoryPlan?: MemoryPlan.default
		sb_memoryLoad.memoryLoad = Math.round(memoryPlan.targetLoad).toInt()
	}
	
	
	
	
}