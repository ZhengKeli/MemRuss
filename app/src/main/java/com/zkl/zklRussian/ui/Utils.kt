package com.zkl.zklRussian.ui

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

abstract class SectionBufferList<T>
constructor(val sectionSize: Int = 20, val sectionCount: Int = 10)
	: AbstractList<T>() {
	
	private val sections = LinkedList<List<T>>()
	private var bufferFrom: Int = 0
	private val bufferSize: Int
		get() {
			return if (sections.isEmpty()) 0
			else sectionSize * (sections.size - 1) + sections.last.size
		}
	private val bufferToExclusive: Int get() = bufferFrom + bufferSize
	@Synchronized override fun get(index: Int): T {
		while (index < bufferFrom) extendAtHead()
		while (index >= bufferToExclusive) appendAtTail()
		return sections[(index - bufferFrom) / sectionSize][(index - bufferFrom) % sectionSize]
	}
	
	@Synchronized private fun extendAtHead() {
		bufferFrom -= sectionSize
		sections.addFirst(getSection(bufferFrom))
		if (sections.size > sectionCount) sections.removeLast()
	}
	
	@Synchronized private fun appendAtTail() {
		sections.addLast(getSection(bufferToExclusive))
		if (sections.size > sectionCount) {
			sections.removeFirst()
			bufferFrom += sectionSize
		}
	}
	
	@Synchronized
	fun clearBuffer() {
		sections.clear()
	}
	
	abstract fun getSection(startFrom: Int): List<T>
	
}


abstract class PendingWorker<in Request : Any, Result> {
	private var pendingRequest: Request? = null
	
	private val lock = ReentrantLock(true)
	private var isSearching = false
	
	fun post(request: Request) {
		lock.withLock {
			pendingRequest = request
			if (!isSearching) {
				startThread()
				isSearching=true
			}
		}
	}
	
	private fun startThread(): Thread {
		return thread {
			while (true) {
				var nullableRequest: Request? = null
				lock.withLock {
					nullableRequest = pendingRequest
					pendingRequest = null
					if (nullableRequest == null) {
						isSearching = false
					}
				}
				val request = nullableRequest ?: break
				val result = onWork(request)
				onDone(request, result)
			}
		}
	}
	
	abstract fun onWork(request: Request): Result
	
	abstract fun onDone(request: Request, result: Result)
	
}
