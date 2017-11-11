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
	private lateinit var tv_daily: TextView
	private lateinit var sb_dailyFill: SeekBar
	private lateinit var tv_maxLoad: TextView
	private lateinit var sb_maxLoad: SeekBar
	private lateinit var b_cancel: Button
	private lateinit var b_ok: Button
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_memory_plan, container, false).apply {
		tv_title = findViewById(R.id.tv_title) as TextView
		tv_daily = findViewById(R.id.tv_dailyFill) as TextView
		sb_dailyFill = findViewById(R.id.sb_dailyFill) as SeekBar
		tv_maxLoad = findViewById(R.id.tv_maxLoad) as TextView
		sb_maxLoad = findViewById(R.id.sb_maxLoad) as SeekBar
		b_cancel = findViewById(R.id.b_cancel) as Button
		b_ok = findViewById(R.id.b_ok) as Button
	}
	
	override fun onStart() {
		super.onStart()
		
		initViews()
		updateViews()
	}
	
	
	val dailyFillRange = 0..50
	var SeekBar.dailyFill: Int
		get() = progress + dailyFillRange.start
		set(value) {
			progress = value
		}
	val maxLoadRange = 0..500
	var SeekBar.maxLoad: Int
		get() = progress + maxLoadRange.start
		set(value) {
			progress = value
		}
	private fun initViews() {
		sb_dailyFill.max = dailyFillRange.run { endInclusive - start }
		sb_dailyFill.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
				tv_daily.text = getString(R.string.dailyLearn_value, seekBar.dailyFill)
			}
			
			override fun onStartTrackingTouch(seekBar: SeekBar) {}
			override fun onStopTrackingTouch(seekBar: SeekBar) {}
		})
		sb_maxLoad.max = maxLoadRange.run { endInclusive - start }
		sb_maxLoad.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
				tv_maxLoad.text = getString(R.string.maxLoad_value, seekBar.maxLoad)
			}
			
			override fun onStartTrackingTouch(seekBar: SeekBar) {}
			override fun onStopTrackingTouch(seekBar: SeekBar) {}
		})
		
		b_cancel.setOnClickListener {
			fragmentManager.popBackStack()
		}
		b_ok.setOnClickListener {
			mutableNotebook.memoryPlan = MemoryPlan(
				dailyFill = sb_dailyFill.dailyFill.toDouble(),
				maxLoad = sb_maxLoad.maxLoad.toDouble()
			)
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
		sb_dailyFill.dailyFill = Math.round(memoryPlan.dailyFill).toInt()
		sb_maxLoad.maxLoad = Math.round(memoryPlan.maxLoad).toInt()
	}
	
	
	
	
}
