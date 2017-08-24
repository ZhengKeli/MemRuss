package com.zkl.ZKLRussian.core.note


data class NoteMemory(
	
	/**
	 * 该词条的学习状态
	 */
	val state: MemoryState = MemoryState.infant,
	
	/**
	 * 该词条的学习进度，
	 * 标准地复习一次 progress + 1.0
	 */
	val progress: Float = 0.0f,
	
	/**
	 * 该词条的负担有多大，
	 * 一般在该阶段指平均一天要复习该词条多少次（可能小于 1）
	 */
	val load: Float = 0.0f,
	
	/**
	 * 下一次需要复习的时间
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 * 用 -1 表示不需要复习
	 */
	val reviewTime: Long = -1

)

enum class MemoryState {
	/**
	 * 该词条还未被加到复习计划中
	 */
	infant,
	/**
	 * 词条已经在复习计划中，且尚未学习完成
	 */
	learning,
	/**
	 * 词条已经在复习计划中，且已学习完成
	 */
	finished
}