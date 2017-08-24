package com.zkl.ZKLRussian.ui

import java.util.*

abstract class SectionBufferList<T>
constructor(val sectionSize:Int = 50,val sectionCount:Int = 10)
	: AbstractList<T>() {
	
	private val sections = LinkedList<List<T>>()
	private var bufferFrom:Int = 0
	private val bufferSize:Int get()= sectionSize*sections.size
	@Synchronized override fun get(index: Int): T {
		val relative = index - bufferFrom
		while (relative < 0) appendFirst()
		while (relative>= bufferSize) appendLast()
		return sections[relative / sectionSize][relative % sectionSize]
	}
	@Synchronized private fun appendFirst(){
		bufferFrom -= sectionSize
		sections.addFirst(getSection(bufferFrom))
		if (sections.size > sectionCount) sections.removeLast()
	}
	@Synchronized private fun appendLast(){
		sections.addLast(getSection(bufferFrom + bufferSize))
		if (sections.size > sectionCount) {
			sections.removeFirst()
			bufferFrom += sectionSize
		}
	}
	
	abstract fun getSection(startFrom:Int):List<T>
	
}