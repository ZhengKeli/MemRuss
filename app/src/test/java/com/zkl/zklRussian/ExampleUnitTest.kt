package com.zkl.zklRussian

import com.zkl.zklRussian.core.note.MemoryAlgorithm
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
	@Test
	fun testAddition() {
		assertEquals(1 + 1, 2)
	}
	
	@Test
	fun testMemoryAlgorithm() {
		var progress = 0.0
		var sumTime = 0L
		var sumLoad = 0.0
		while (progress < 300.0) {
			val oldProgress = progress
			val reviewInterval = MemoryAlgorithm.computeReviewInterval(progress)
			val load = MemoryAlgorithm.computeLoad(reviewInterval)
			sumTime += reviewInterval
			sumLoad += load
			progress += MemoryAlgorithm.progressUnit
			
			
			println("""[$oldProgress -> $progress]:
				interval=${reviewInterval / (3600.0 * 1000.0 * 24)} days
				sumTime=${sumTime / (3600.0 * 1000.0 * 24)} days
				load=$load
				sumLoad=$sumLoad
				
				""".replaceIndent()
			)
		}
		
	}
}