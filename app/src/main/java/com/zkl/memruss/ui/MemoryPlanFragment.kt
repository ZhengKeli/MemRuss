package com.zkl.memruss.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.zkl.memruss.R
import com.zkl.memruss.control.myApp
import com.zkl.memruss.control.note.NotebookKey
import com.zkl.memruss.core.note.MutableNotebook
import com.zkl.memruss.core.note.base.MemoryPlan
import com.zkl.memruss.core.note.base.NotebookMemoryStatus.INFANT
import com.zkl.memruss.core.note.base.NotebookMemoryStatus.LEARNING
import kotlinx.android.synthetic.main.fragment_memory_plan.*
import kotlin.math.roundToInt

class MemoryPlanFragment : Fragment(),
	MemoryPlanDropDialog.MemoryPlanDroppedListener {
	
	companion object {
		fun newInstance(notebookKey: NotebookKey) = MemoryPlanFragment::class.java.newInstance(notebookKey)
	}
	
	private val dailyReviewsRange = 0..500
	private val dailyNewWordsRange = 0..100
	private fun SeekBar.getValueInRange(range: IntRange): Int {
		return range.run { start + (endInclusive - start) * progress / max }
	}
	
	private fun SeekBar.setValueInRange(range: IntRange, value: Int) {
		progress = range.run { max * (value - start) / (endInclusive - start) }
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.fragment_memory_plan, container, false)
	}
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		val notebookKey = argNotebookKey
		val notebook = myApp.notebookShelf.restoreNotebook(notebookKey)
		val mutableNotebook = notebook as MutableNotebook
		
		val memoryState = notebook.memoryState
		val memoryPlan = notebook.memoryPlan ?: MemoryPlan.default
		
		tv_title.text = when (memoryState.status) {
			INFANT -> getString(R.string.make_MemoryPlan)
			LEARNING -> getString(R.string.MemoryPlan)
		}
		
		if (memoryState.status == INFANT) {
			tv_info.visibility = View.GONE
		} else {
			val countTotal = notebook.noteCount
			val countNotInPlan = notebook.countNeedActivateNotes()
			val countInPlan = countTotal - countNotInPlan
			tv_info.text = getString(R.string.memory_plan_statistics, countTotal, countInPlan, countNotInPlan)
				.replace("\\n", "\n")
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
				tv_dailyNewWords.text = getString(R.string.daily_newWords_SettingTitle, seekBar.getValueInRange(dailyNewWordsRange))
			}
			
			override fun onStartTrackingTouch(seekBar: SeekBar) {}
			override fun onStopTrackingTouch(seekBar: SeekBar) {}
		})
		
		if (memoryState.status == INFANT) {
			b_dropMemoryPlan.visibility = View.GONE
		} else {
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
