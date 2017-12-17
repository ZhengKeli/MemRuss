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

class SectionBuffer<T>(val sectionSize: Int = 128):AbstractList<T>() {
	
	override val size get() = bufferSize
	override fun get(index: Int) = buffer[index / sectionSize][index % sectionSize]
	@Synchronized fun getSizeAndExpand(): Int {
		if (bufferSize == 0 && sourceSize > 0) expandBuffer()
		return bufferSize
	}
	@Synchronized fun getAndExpand(index: Int): T {
		if (index >= bufferSize) expandBuffer()
		return get(index)
	}
	
	private var sourceSize: Int = 0
	private var source: ((offset: Int, limit: Int) -> List<T>)? = null
	fun setSource(sourceSize: Int, source: (offset: Int, limit: Int) -> List<T>) {
		clearBuffer()
		this.sourceSize = sourceSize
		this.source = source
	}
	
	private val buffer = LinkedList<List<T>>()
	private var bufferSize:Int = 0
	@Synchronized fun expandBuffer(): Boolean {
		val requireSize = Math.min(sectionSize, sourceSize - bufferSize)
		if (requireSize <= 0) return false
		
		val section = source?.invoke(bufferSize, requireSize) ?: return false
		if (section.size < requireSize) throw RuntimeException("Section size not enough!")
		
		buffer.addLast(section)
		bufferSize += requireSize
		return true
	}
	@Synchronized fun clearBuffer() {
		buffer.clear()
		bufferSize = 0
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
