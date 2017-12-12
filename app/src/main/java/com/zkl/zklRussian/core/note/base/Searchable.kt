package com.zkl.zklRussian.core.note.base

interface SearchableNotebook<Note : SearchableNote<*>> : BaseNotebook<Note> {
	
	/**
	 * 根据关键词搜索一些词条
	 * @param keyword 关键词，若为空则此方法等同于[selectLatestNotes]
	 * @param count 获取的最大词条数
	 * @param offset 可以跳过最开始的几个词条，返回后面的几个
	 * @return 返回搜索结果，按照修改日期倒序排序
	 */
	fun selectByKeyword(keyword: String, count: Int = 100, offset: Int = 0): List<Note>
	
}

interface SearchableNote<out Content : SearchableContent> : BaseNote<Content>

interface SearchableContent : BaseContent {
	
	/**
	 * 用于被搜索的标签
	 */
	val searchTags: Collection<String>
	
}