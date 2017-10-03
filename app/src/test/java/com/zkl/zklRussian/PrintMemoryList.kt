package com.zkl.zklRussian

import com.zkl.zklRussian.core.note.MemoryAlgorithm

fun main(args: Array<String>) {
	var progress = 0.0
	var sumTime = 0L
	while (progress<25.0) {
		val oldProgress = progress
		val reviewInterval = MemoryAlgorithm.computeReviewInterval(progress)
		sumTime += reviewInterval
		progress += MemoryAlgorithm.progressUnit
		
		println(
			"[$oldProgress -> $progress]: " +
				"interval=${reviewInterval / (3600.0 * 1000.0* 24)} days" + "  "+
				"sum=${sumTime / (3600.0 * 1000.0 * 24)} days")
		
	}
}