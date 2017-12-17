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

abstract class SectionBuffer<T>
constructor(val sectionSize: Int = 128, var paddingSize: Int = sectionSize / 4) {
	
	abstract fun onExpand(offset: Int, limit: Int): List<T>
	
	private var reachTail:Boolean = false
	private val sections = LinkedList<List<T>>()
	private val bufferSize:Int get() {
		return if (!reachTail) sectionSize * sections.size
		else sectionSize * (sections.size - 1) + sections.last.size
	}
	
	val size:Int get() {
		if(sections.isEmpty()) expand()
		return bufferSize
	}
	@Synchronized operator fun get(index: Int): T {
		if (bufferSize - index < paddingSize) expand()
		return sections[index / sectionSize][index % sectionSize]
	}
	@Synchronized private fun expand(): Boolean {
		if (reachTail) return false
		val append = onExpand(bufferSize, sectionSize)
		if (append.size < sectionSize) reachTail = true
		sections.addLast(append)
		return true
	}
	@Synchronized fun clear() {
		sections.clear()
		reachTail = false
	}
	
	
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
