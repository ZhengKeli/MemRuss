package com.zkl.zklRussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.note.NotebookKey
import com.zkl.zklRussian.core.note.base.MemoryPlan
import com.zkl.zklRussian.core.note.base.NotebookMemoryStatus
import kotlinx.android.synthetic.main.fragment_memory_plan.*
import kotlin.math.roundToInt

class MemoryPlanFragment : NotebookHoldingFragment(),
	MemoryPlanDropDialog.MemoryPlanDroppedListener {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey)
			= MemoryPlanFragment::class.java.newInstance(notebookKey)
	}
	
	private val dailyReviewsRange = 0..500
	private val dailyNewWordsRange = 0..100
	private fun SeekBar.getValueInRange(range: IntRange): Int {
		return range.run { start + (endInclusive - start) * progress / max }
	}
	private fun SeekBar.setValueInRange(range: IntRange, value: Int) {
		progress = range.run { max * (value - start) / (endInclusive - start) }
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater.inflate(R.layout.fragment_memory_plan, container, false)
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		val memoryState = notebook.memoryState
		val memoryPlan = notebook.memoryPlan ?: MemoryPlan.default
		
		tv_title.text = when (memoryState.status) {
			NotebookMemoryStatus.infant -> getString(R.string.make_MemoryPlan)
			NotebookMemoryStatus.learning -> getString(R.string.MemoryPlan)
			NotebookMemoryStatus.paused -> getString(R.string.MemoryPlan_paused)
		}
		
		tv_dailyReviews.text = getString(R.string.daily_reviews_SettingTitle, memoryPlan.dailyReviews.roundToInt())
		sb_dailyReviews.max = dailyReviewsRange.run { endInclusive - start }
		sb_dailyReviews.setValueInRange(dailyReviewsRange, Math.round(memoryPlan.dailyReviews).toInt())
		sb_dailyReviews.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
				tv_dailyReviews.text = getString(R.string.daily_reviews_SettingTitle, seekBar.getValueInRange(dailyReviewsRange))
			}
			
			override fun onStartTrackingTouch(seekBar: SeekBar) {}
			override fun onStopTrackingTouch(seekBar: SeekBar) {}
		})
		
		tv_dailyNewWords.text = getString(R.string.daily_newWords_SettingTitle, memoryPlan.dailyNewWords.roundToInt())
		sb_dailyNewWords.max = dailyNewWordsRange.run { endInclusive - start }
		sb_dailyNewWords.setValueInRange(dailyNewWordsRange, Math.round(memoryPlan.dailyNewWords).toInt())
		sb_dailyNewWords.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
				tv_dailyNewWords.text = getString(R.string.daily_newWords_SettingTitle,seekBar.getValueInRange(dailyNewWordsRange))
			}
			
			override fun onStartTrackingTouch(seekBar: SeekBar) {}
			override fun onStopTrackingTouch(seekBar: SeekBar) {}
		})
		
		if(memoryState.status== NotebookMemoryStatus.infant){
			b_dropMemoryPlan.visibility = View.GONE
		}
		else {
			b_dropMemoryPlan.setOnClickListener {
				MemoryPlanDropDialog.newInstance(notebookKey, this).show(fragmentManager)
			}
		}
		
		b_cancel.setOnClickListener {
			fragmentManager.popBackStack()
		}
		b_ok.setOnClickListener {
			val isCreating = notebook.memoryPlan == null
			val newMemoryPlan = MemoryPlan(
				dailyReviews = sb_dailyReviews.getValueInRange(dailyReviewsRange).toDouble(),
				dailyNewWords = sb_dailyNewWords.getValueInRange(dailyNewWordsRange).toDouble()
			)
			
			mutableNotebook.memoryPlan = newMemoryPlan
			if (isCreating) mutableNotebook.activateNotes(newMemoryPlan.dailyNewWords.roundToInt())
			else mutableNotebook.activateNotesByPlan()
			
			fragmentManager.popBackStack()
		}
		
	}
	
	override fun onMemoryPlanDropped() {
		fragmentManager.popBackStack()
	}
	
}
