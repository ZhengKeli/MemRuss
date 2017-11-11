package com.zkl.zklRussian.control.note

import com.zkl.zklRussian.core.note.*

class Notebook2:Notebook{
	override val version: Int
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val name: String
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val memoryPlan: MemoryPlan?
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val memorySummary: MemorySummary
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val memoryState: NotebookMemoryState
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val noteCount: Int
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	
	override fun getNote(noteId: Long): Note {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun selectLatestNotes(count: Int, offset: Int): List<Note> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun selectNeedReviewNotes(nowTime: Long, asc: Boolean, count: Int, offset: Int): List<Note> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun selectByKeyword(keyword: String, count: Int, offset: Int): List<Note> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun checkUniqueTag(tag: String, exceptId: Long): Long {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun close() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
}