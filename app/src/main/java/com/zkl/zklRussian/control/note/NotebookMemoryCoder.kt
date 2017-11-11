package com.zkl.zklRussian.control.note

import com.zkl.zklRussian.core.note.MemoryPlan
import com.zkl.zklRussian.core.note.NotebookMemoryState
import com.zkl.zklRussian.core.note.NotebookMemoryStatus
import org.json.JSONObject


object NotebookMemoryCoder {
	 fun encode(memoryState: NotebookMemoryState)
		 = memoryState.let {
		 JSONObject(mapOf(
			 it::status.run { name to get().name },
			 it::lastRefillTime.run { name to get() },
			 it::lastResumeTime.run { name to get() },
			 it::lastPauseTime.run { name to get() }
		 )).toString()
	 }
	
	fun decode(string: String)
		= JSONObject(string).run {
		NotebookMemoryState(
			status = NotebookMemoryStatus.valueOf(
				getString(NotebookMemoryState::status.name)),
			lastRefillTime = getLong(NotebookMemoryState::lastRefillTime.name),
			lastResumeTime = getLong(NotebookMemoryState::lastResumeTime.name),
			lastPauseTime = getLong(NotebookMemoryState::lastPauseTime.name)
		)
	}
}

object MemoryPlanCoder {
	fun encode(memory: MemoryPlan)
		= memory.let {
		JSONObject(mapOf(
			it::refillFrequency.run { name to get() },
			it::maxLoad.run { name to get() },
			it::minLoad.run { name to get() },
			it::fulfillProgress.run { name to get() }
		)).toString()
	}
	
	fun decode(string: String): MemoryPlan
		= JSONObject(string).run {
		MemoryPlan(
			refillFrequency = getDouble(MemoryPlan::refillFrequency.name),
			maxLoad = getDouble(MemoryPlan::maxLoad.name),
			minLoad = getDouble(MemoryPlan::minLoad.name),
			fulfillProgress = getDouble(MemoryPlan::fulfillProgress.name)
		)
	}
}
