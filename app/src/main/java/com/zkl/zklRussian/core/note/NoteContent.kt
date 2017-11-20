package com.zkl.zklRussian.core.note

import java.io.Serializable


interface NoteContent:Serializable {
	/**
	 * 不同种类的[NoteContent]的类型标签
	 */
	val typeTag: String get() = this.javaClass.simpleName
	
	/**
	 * 用于被搜索的标签
	 */
	val searchTags: Collection<String>
	
	/**
	 * 用于辨识重复的标签
	 * 在同一个[Notebook]里不允许有两个词条含有相同的 uniqueTag
	 */
	val uniqueTags: Collection<String>
		get() = untypedUniqueTags.map { typeTag + ":" + it }
	
	/**
	 * 不带类型信息的重复辨识标签
	 */
	val untypedUniqueTags: Collection<String>
}

