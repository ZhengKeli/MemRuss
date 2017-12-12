package com.zkl.zklRussian.core.note.base


interface MemoryNotebook<Note : MemoryNote<*>> : BaseNotebook<Note> {
	
	/**
	 * 该单词本的复习状态
	 */
	val memoryState: NotebookMemoryState
	
	/**
	 * 该单词本的复习计划
	 */
	val memoryPlan: MemoryPlan?
	
	/**
	 * 总负荷
	 * 用平均每天要复习的次数表示，
	 * 用来帮助判断应该如何加入新词
	 */
	val sumMemoryLoad: Double
	
	/**
	 * 统计有多少词条需要复习
	 * （只有处于正在复习状态的词条会被检索出来）
	 */
	fun countNeedReviewNotes(nowTime: Long): Int
	
	/**
	 * 根据需要复习的时间检索词条
	 * （只有处于正在复习状态的词条会被检索出来）
	 */
	fun selectNeedReviewNotes(nowTime: Long, asc: Boolean = false, count: Int = 1, offset: Int = 0): List<Note>
	
}

interface MutableMemoryNotebook<Content : BaseContent, Note : MemoryNote<Content>> : MemoryNotebook<Note>,
	MutableBaseNotebook<Content, Note> {
	
	/**
	 * 修改 note 的复习进度
	 */
	fun modifyNoteMemory(noteId: Long, memoryState: NoteMemoryState)
	
	/**
	 * 该单词本的复习计划
	 * 若将其设为 null 则会重设所有单词的状态
	 */
	override var memoryPlan: MemoryPlan?
	
	/**
	 * 按照复习计划激活词条
	 * 如果还没有计划就什么也不做
	 * 如果有计划了就要激活一定数量的词条并更新[memoryState]
	 */
	fun activateNotesByPlan(nowTime: Long = System.currentTimeMillis()): Int
	
	/**
	 * 直接激活词条
	 * 此操作可以无视[memoryState]
	 * 无论是否有复习计划都会进行
	 */
	fun activateNotes(count: Int = 1, nowTime: Long = System.currentTimeMillis()): Int
}

interface MemoryNote<out Content : BaseContent> : BaseNote<Content> {
	
	/**
	 * 词条的记忆状态
	 */
	val memoryState: NoteMemoryState
	
	/**
	 * 词条记忆状态最后一次被修改的毫秒时间戳
	 */
	val memoryUpdateTime: Long
	
}

data class MemoryPlan(
	
	/**
	 * 平均每天要复习的次数表示，
	 * 只要实际工作量低于它，
	 * 程序就会以每天[dailyNewWords]个的速度不断添加新词
	 */
	val dailyReviews: Double,
	
	/**
	 * 激活词条的速度
	 * 当实际工作量低于[dailyReviews]时，
	 * 程序就会以每天[dailyNewWords]个的速度不断激活新词
	 */
	val dailyNewWords: Double

) {
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
	 * 该单词本学习计划的状态
	 */
	val status: NotebookMemoryStatus,
	
	/**
	 * 上次激活新词的时间
	 * 用于计算应该添加多少新词
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 * 用 -1 表示不需要激活新词
	 */
	val lastActivateTime: Long,
	
	/**
	 * 上次开始计划的时间
	 * 在计划被制定并生效时、暂停后被恢复时记录
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 */
	val lastResumeTime: Long,
	
	/**
	 * 上次暂停的时间
	 * 用于在恢复时计算暂停了多久，
	 * 并据此进行一些变动
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 * 用 -1 表示从没暂停过
	 */
	val lastPauseTime: Long

) {
	companion object {
		val infantState =
			NotebookMemoryState(NotebookMemoryStatus.infant, -1L, -1L, -1L)
		
		fun beginningState(nowTime: Long = System.currentTimeMillis())
			= NotebookMemoryState(NotebookMemoryStatus.learning, nowTime, nowTime, -1L)
	}
}

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
	val status: NoteMemoryStatus,
	
	/**
	 * 该词条的学习进度，
	 * 标准地复习一次 progress + 1.0
	 */
	val progress: Double,
	
	/**
	 * 该词条的工作量有多大，
	 * 一般在该阶段指平均一天要复习该词条多少次（可能小于 1），
	 * 单个单词最大工作量应该为 3
	 */
	val load: Double,
	
	/**
	 * 下一次需要复习的时间
	 * （为了避免因为时区变化而引起的错误，统一使用GMT+0毫秒时间)
	 * 用 -1 表示不需要复习
	 */
	val reviewTime: Long

) {
	companion object {
		fun infantState()
			= NoteMemoryState(NoteMemoryStatus.infant, 0.0, 0.0, -1L)
		
		fun beginningState(nowTime: Long = System.currentTimeMillis())
			= NoteMemoryState(NoteMemoryStatus.learning, 0.0, MemoryAlgorithm.maxSingleLoad, nowTime)
	}
}

object MemoryAlgorithm {
	
	//规定
	
	val progressUnit: Double = 1.0
	
	val maxSingleLoad: Double = 5.0
	
	
	//参数
	
	/**
	 * 记忆曲线线性因子 k
	 * 与复习时间间隔成正比
	 */
	val arg_k: Double = 1.0
	
	/**
	 * 记忆曲线指数因子 a
	 * exp(a)与复习时间间隔成正比
	 */
	val arg_ex: Double = 2.0
	
	/**
	 * 随机乱序参数，
	 * 复习时间间隔将以该参数的规模上下随机浮动：
	 * ΔT ∈ ΔT * (1.0 ± arg_ran)
	 */
	val arg_ran: Double = 0.07
	
	/**
	 * 惩罚参数
	 * 当某个词没记住时要加倍减少工作进度，
	 * 该参数表示该倍数
	 */
	val arg_pu: Double = 2.0
	
	
	//函数
	
	fun computeReviewInterval(progress: Double): Long {
		val random = 1.0 + (Math.random() * 2.0 - 1.0) * arg_ran
		return (arg_k * Math.pow(progress, arg_ex) * 1000.0 * 3600.0 * random).toLong()
	}
	
	fun computeLoad(reviewInterval: Long): Double {
		return ((24.0 * 3600 * 1000) / reviewInterval).coerceAtMost(maxSingleLoad)
	}
	
}

fun NoteMemoryState.getNextReviewTime(learned: Boolean, nowTime: Long = System.currentTimeMillis()): NoteMemoryState {
	val nextProgress =
		if (learned) progress + MemoryAlgorithm.progressUnit
		else (progress - MemoryAlgorithm.progressUnit * MemoryAlgorithm.arg_pu).coerceAtLeast(0.0)
	val reviewInterval = MemoryAlgorithm.computeReviewInterval(nextProgress)
	val nextReviewTime = nowTime + reviewInterval
	val nextLoad = MemoryAlgorithm.computeLoad(reviewInterval)
	return copy(
		progress = nextProgress,
		reviewTime = nextReviewTime,
		load = nextLoad
	)
}