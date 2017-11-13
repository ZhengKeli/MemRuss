package com.zkl.zklRussian.core.note


enum class NoteMemoryStatus {
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
data class NoteMemoryState(
	
	/**
	 * 该词条的学习阶段
	 */
	val status: NoteMemoryStatus = NoteMemoryStatus.infant,
	
	/**
	 * 该词条的学习进度，
	 * 标准地复习一次 progress + 1.0
	 */
	val progress: Double = 0.0,
	
	/**
	 * 该词条的工作量有多大，
	 * 一般在该阶段指平均一天要复习该词条多少次（可能小于 1），
	 * 单个单词最大工作量应该为 3
	 */
	val load: Double = 0.0,
	
	/**
	 * 下一次需要复习的时间
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 * 用 -1 表示不需要复习
	 */
	val reviewTime: Long = -1

){
	companion object {
		fun beginningState(nowTime:Long=System.currentTimeMillis())
			= NoteMemoryState(NoteMemoryStatus.learning,0.0,MemoryAlgorithm.maxSingleLoad,nowTime)
	}
}
