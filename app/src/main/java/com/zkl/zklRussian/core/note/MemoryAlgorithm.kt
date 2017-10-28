package com.zkl.zklRussian.core.note

object MemoryAlgorithm {
	
	//规定
	
	val progressUnit:Double = 1.0
	
	val maxSingleLoad:Double = 3.0
	
	
	//参数
	
	/**
	 * 记忆曲线线性因子 k
	 * 与复习时间间隔成正比
	 */
	val arg_k:Double = 1.0
	
	/**
	 * 记忆曲线指数因子 a
	 * exp(a)与复习时间间隔成正比
	 */
	val arg_ex:Double = 2.0
	
	/**
	 * 随机乱序参数，
	 * 复习时间间隔将以该参数的规模上下随机浮动：
	 * ΔT ∈ ΔT * (1.0 ± arg_ran)
	 */
	val arg_ran:Double = 0.07
	
	/**
	 * 惩罚参数
	 * 当某个词没记住时要加倍减少工作进度，
	 * 该参数表示该倍数
	 */
	val arg_pu:Double = 2.0
	
	
	//函数
	
	fun computeReviewInterval(progress: Double): Long {
		val random = 1.0 + (Math.random() * 2.0 - 1.0) * arg_ran
		return (arg_k * Math.pow(progress, arg_ex) * 1000.0 * 3600.0 * random).toLong()
	}
	
	fun computeLoad(reviewInterval: Long): Double {
		return ((24.0 * 3600 * 1000) / reviewInterval).coerceAtMost(maxSingleLoad)
	}
	
}

fun NoteMemory.getNextReviewTime(learned: Boolean, nowTime: Long = System.currentTimeMillis()): NoteMemory {
	val nextProgress =
		if (learned) progress + MemoryAlgorithm.progressUnit
		else progress - MemoryAlgorithm.progressUnit * MemoryAlgorithm.arg_pu
	val reviewInterval = MemoryAlgorithm.computeReviewInterval(nextProgress)
	val nextReviewTime = nowTime + reviewInterval
	val nextLoad = MemoryAlgorithm.computeLoad(reviewInterval)
	return copy(
		progress = nextProgress,
		reviewTime = nextReviewTime,
		load = nextLoad
	)
}


