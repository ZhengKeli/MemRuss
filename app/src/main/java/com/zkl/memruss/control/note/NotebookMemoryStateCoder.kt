package com.zkl.memruss.control.note

import com.zkl.memruss.core.note.base.MemoryPlan
import com.zkl.memruss.core.note.base.NotebookMemoryState
import com.zkl.memruss.core.note.base.NotebookMemoryStatus
import org.json.JSONObject


object NotebookMemoryStateCoder {
	 fun encode(memoryState: NotebookMemoryState)
		 = memoryState.let {
		 JSONObject(mapOf(
			 it::status.run { name to get().name },
			 it::planLaunchTime.run { name to get() },
			 it::lastActivateTime.run { name to get() }
		 )).toString()
	 }
	
	fun decode(string: String)
		= JSONObject(string).run {
		NotebookMemoryState(
			status = NotebookMemoryStatus.valueOf(
				getString(NotebookMemoryState::status.name)),
			lastActivateTime = getLong(NotebookMemoryState::lastActivateTime.name),
			planLaunchTime = getLong(NotebookMemoryState::planLaunchTime.name)
		)
	}
}

object MemoryPlanCoder {
	fun encode(memory: MemoryPlan)
		= memory.let {
		JSONObject(mapOf(
			it::dailyReviews.run { name to get() },
			it::dailyNewWords.run { name to get() }
		)).toString()
	}
	
	fun decode(string: String): MemoryPlan
		= JSONObject(string).run {
		MemoryPlan(
			dailyReviews = getDouble(MemoryPlan::dailyReviews.name),
			dailyNewWords = getDouble(MemoryPlan::dailyNewWords.name)
		)
	}
}
