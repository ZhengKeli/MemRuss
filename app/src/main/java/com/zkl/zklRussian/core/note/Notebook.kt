package com.zkl.zklRussian.core.note

import java.io.Closeable

interface Notebook:Closeable {
	
	//info
	
	/**
	 * 该单词本版本
	 */
	val version:Int
	
	/**
	 * 该单词本的名字
	 */
	val name: String
	
	
	
	//notes
	
	/**
	 * 词条总数量
	 */
	val noteCount: Int
	
	/**
	 * 根据 noteId 获取一个词条
	 * @return 对应 noteId 的词条
	 * @throws NoteIdNotFoundException 当对应 noteId 的词条没找到时抛出错误
	 */
	@Throws(NoteIdNotFoundException::class)
	fun getNote(noteId: Long): Note
	
	/**
	 * 获得最新的一些词条
	 * @param count 获取的最大词条数
	 * @param offset 可以跳过最开始的几个词条，返回后面的几个
	 * @return 最新的一些词条，按照修改日期倒序排序
	 */
	fun selectLatestNotes(count: Int = 100, offset: Int = 0): List<Note>
	
	
	/**
	 * 根据关键词搜索一些词条
	 * @param keyword 关键词，若为空则此方法等同于[selectLatestNotes]
	 * @param count 获取的最大词条数
	 * @param offset 可以跳过最开始的几个词条，返回后面的几个
	 * @return 返回搜索结果，按照修改日期倒序排序
	 */
	fun selectByKeyword(keyword: String, count: Int=100, offset: Int=0): List<Note>
	
	/**
	 * 搜索是否存在某个 uniqueTag
	 */
	fun checkUniqueTag(tag: String,exceptId:Long=-1L): Long
	
	/**
	 * 批量搜索是否存在某些 uniqueTag
	 */
	@Throws(ConflictException::class)
	fun throwIfDuplicated(uniqueTags: Collection<String>,exceptId: Long=-1L){
		uniqueTags.forEach { uniqueTag ->
			val id = checkUniqueTag(uniqueTag,exceptId)
			if (id != -1L) throw ConflictException(uniqueTag, id)
		}
	}
	
	
	
	//memory
	
	/**
	 * 该笔记本的复习状态
	 */
	val memoryState: NotebookMemoryState
	
	/**
	 * 该笔记本的复习计划
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
	fun countNeedReviewNotes(nowTime:Long):Int
	
	/**
	 * 根据需要复习的时间检索词条
	 * （只有处于正在复习状态的词条会被检索出来）
	 */
	fun selectNeedReviewNotes(nowTime:Long, asc: Boolean = false, count: Int = 1, offset: Int = 0): List<Note>
	
}

interface MutableNotebook : Notebook {
	
	//transaction
	
	/**
	 * 执行一个批量操作。若操作中抛出任何错误则会回滚。
	 * @return 操作是否无误地成功了。
	 */
	fun withTransaction(action: () -> Unit)
	
	
	//info
	override var name:String
	
	
	//notes
	
	/**
	 * 添加一个词条
	 * @return 返回刚加入的词条的 noteId
	 */
	@Throws(ConflictException::class)
	fun addNote(content: NoteContent, memoryState: NoteMemoryState? = null): Long
	
	/**
	 * 添加一堆词条
	 */
	@Throws(ConflictException::class)
	fun addNotes(drafts: Collection<NoteContent>) {
		drafts.forEach { addNote(it) }
	}
	
	/**
	 * 根据 noteId 删除一个词条
	 */
	fun deleteNote(noteId: Long)
	
	/**
	 * 修改 note 的内容
	 */
	@Throws(ConflictException::class)
	fun modifyNoteContent(noteId: Long, content: NoteContent)
	
	/**
	 * 修改 note 的复习进度
	 */
	fun modifyNoteMemory(noteId: Long, memoryState: NoteMemoryState)
	
	
	//memory
	
	/**
	 * 复习计划
	 * 若将其设为 null 则会重设所有单词的状态
	 */
	override var memoryPlan: MemoryPlan?
	
	fun fillNotes(count: Int = 1, nowTime: Long=System.currentTimeMillis())
	
	fun fillNotesByPlan() {
		if (memoryState.status != NotebookMemoryStatus.learning) return
		val targetLoad = memoryPlan?.targetLoad ?: 0.0
		//todo check last fill time
		val sumLoad = sumMemoryLoad
		val addCount = ((targetLoad - sumLoad) / MemoryAlgorithm.maxSingleLoad).toInt()
		if(addCount>0) fillNotes(addCount)
	}
	
}


