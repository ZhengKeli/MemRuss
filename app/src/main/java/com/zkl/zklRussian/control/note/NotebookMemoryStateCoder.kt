package com.zkl.zklRussian.control.note

import com.zkl.zklRussian.core.note.MemoryPlan
import com.zkl.zklRussian.core.note.NotebookMemoryState
import com.zkl.zklRussian.core.note.NotebookMemoryStatus
import org.json.JSONObject


object NotebookMemoryStateCoder {
	 fun encode(memoryState: NotebookMemoryState)
		 = memoryState.let {
		 JSONObject(mapOf(
			 it::status.run { name to get().name },
			 it::lastFillTime.run { name to get() },
			 it::lastResumeTime.run { name to get() },
			 it::lastPauseTime.run { name to get() }
		 )).toString()
	 }
	
	fun decode(string: String)
		= JSONObject(string).run {
		NotebookMemoryState(
			status = NotebookMemoryStatus.valueOf(
				getString(NotebookMemoryState::status.name)),
			lastFillTime = getLong(NotebookMemoryState::lastFillTime.name),
			lastResumeTime = getLong(NotebookMemoryState::lastResumeTime.name),
			lastPauseTime = getLong(NotebookMemoryState::lastPauseTime.name)
		)
	}
}

object MemoryPlanCoder {
	fun encode(memory: MemoryPlan)
		= memory.let {
		JSONObject(mapOf(
			it::targetLoad.run { name to get() },
			it::fillFrequency.run { name to get() }
		)).toString()
	}
	
	fun decode(string: String): MemoryPlan
		= JSONObject(string).run {
		MemoryPlan(
			targetLoad = getDouble(MemoryPlan::targetLoad.name),
			fillFrequency = getDouble(MemoryPlan::fillFrequency.name)
		)
	}
}
