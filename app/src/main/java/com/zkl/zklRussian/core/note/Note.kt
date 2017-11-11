package com.zkl.zklRussian.core.note


data class Note (
	
	//basic info
	
	/**
	 * 在同一本笔记本中区别于其他词条的 id
	 */
	val id: Long,
	
	/**
	 * 词条被创建的时间的毫秒时间戳
	 */
	val createTime: Long,
	
	
	//content
	
	/**
	 * 词条的内容，其类型可能会不同
	 */
	val content: NoteContent,
	
	/**
	 * 词条内容最后一次被修改的毫秒时间戳
	 */
	val contentUpdateTime: Long,
	
	
	//memory
	
	/**
	 * 词条的记忆状态
	 */
	val memoryState: NoteMemoryState,
	
	/**
	 * 词条记忆状态最后一次被修改的毫秒时间戳
	 */
	val memoryUpdateTime: Long

)



