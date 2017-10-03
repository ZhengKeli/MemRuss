package com.zkl.zklRussian.core.note


data class MemoryPlan(
	
	/**
	 * 加入新单词的速度
	 * 用平均每天加入的新词数量表示
	 */
	val refillFrequency:Double,
	
	/**
	 * 最大工作量
	 * 用平均每天要复习的次数表示，
	 * 当实际工作量超过它的时候，系统将停止加入新词
	 */
	val maxLoad:Double = 200.0,
	
	/**
	 * 最小工作量
	 * 用平均每天要复习的次数表示，
	 * 当实际工作量低于它时，系统将立即加入新词
	 */
	val minLoad:Double = 0.0,
	
	/**
	 * 圆满学习进度
	 * 当某词条的学习进度超过该值时，系统将不会再要求复习该词
	 */
	val fulfillProgress:Double = 25.0
	
)

enum class NotebookMemoryState {
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
	paused,
	/**
	 * 该单词本目前的所有单词都完成学习了
	 */
	finished
}
data class NotebookMemory(
	
	/**
	 * 该单词本的学习阶段
	 */
	val state: NotebookMemoryState,
	
	/**
	 * 上次填充新词的时间
	 * 用于计算应该添加多少新词
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 * 用 -1 表示不需要填充新词
	 */
	val lastRefillTime:Long,
	
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
		val infantInstance =
			NotebookMemory(NotebookMemoryState.infant, -1L, -1L, -1L)
	}
}

data class MemorySummary(
	
	/**
	 * 总负荷
	 * 用平均每天要复习的次数表示，
	 * 用来帮助判断应该如何加入新词
	 */
	val sumLoad:Float=0f
)