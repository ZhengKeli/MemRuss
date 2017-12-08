package com.zkl.zklRussian.core.note


data class MemoryPlan(
	
	/**
	 * 最大工作量
	 * 用平均每天要复习的次数表示，
	 * 只要实际工作量低于它，
	 * 程序就会以每天[activateFrequency]个的速度不断添加新词
	 */
	val targetLoad:Double,
	
	/**
	 * 激活词条的速度
	 * 当实际工作量低于[targetLoad]时，
	 * 程序就会以每天[activateFrequency]个的速度不断激活新词
	 */
	val activateFrequency:Double

){
	companion object {
		val default = MemoryPlan(100.0, 10.0)
	}
}

enum class NotebookMemoryStatus {
	/**
	 * 该单词本尚未制定学习计划
	 */
	infant,
	/**
	 * 该单词本的学习计划正在进行中
	 */
	learning,
	/**
	 * 该单词本的学习计划被暂停了
	 */
	paused
}
data class NotebookMemoryState(
	
	/**
	 * 该单词本的学习阶段
	 */
	val status: NotebookMemoryStatus,
	
	/**
	 * 上次激活新词的时间
	 * 用于计算应该添加多少新词
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 * 用 -1 表示不需要激活新词
	 */
	val lastActivateTime:Long,
	
	/**
	 * 上次开始计划的时间
	 * 在计划被制定并生效时、暂停后被恢复时记录
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 */
	val lastResumeTime:Long,
	
	/**
	 * 上次暂停的时间
	 * 用于在恢复时计算暂停了多久，
	 * 并据此进行一些变动
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 * 用 -1 表示从没暂停过
	 */
	val lastPauseTime:Long

){
	companion object {
		val infantState =
			NotebookMemoryState(NotebookMemoryStatus.infant, -1L, -1L, -1L)
		
		fun beginningState(nowTime: Long = System.currentTimeMillis())
			= NotebookMemoryState(NotebookMemoryStatus.learning, 0, nowTime, -1L)
	}
}

