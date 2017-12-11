package com.zkl.zklRussian.ui

import android.util.SparseArray
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import org.jetbrains.anko.inputMethodManager
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
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
				isSearching = true
			}
		}
	}
	
	private fun startThread(): Thread {
		return thread {
			var lastRequest: Request? = null
			var lastResult: Result? = null
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
				lastRequest = request
				lastResult = result
			}
			if (lastRequest != null && lastResult != null) {
				onAllDone(lastRequest, lastResult)
			}
		}
	}
	
	abstract fun onWork(request: Request): Result
	
	abstract fun onDone(request: Request, result: Result)
	
	abstract fun onAllDone(lastRequest: Request, lastResult: Result)
	
}

class AutoIndexMap<T>{
	private var nextKey = AtomicInteger(0)
	private val sparseArray = SparseArray<T>()
	
	fun put(value: T): Int {
		val key = nextKey.getAndAdd(1)
		sparseArray.put(key, value)
		return key
	}
	fun get(key: Int): T? = sparseArray.get(key)
	fun remove(key: Int): T? {
		val re = sparseArray.get(key)
		sparseArray.remove(key)
		return re
	}
	
}


fun EditText.showSoftInput(forced:Boolean = false) {
	context.inputMethodManager
		.showSoftInput(this,
			if (forced) InputMethodManager.SHOW_FORCED
			else InputMethodManager.SHOW_IMPLICIT
		)
}
