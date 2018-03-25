package com.zkl.memruss.ui

import android.util.SparseArray
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import org.jetbrains.anko.inputMethodManager
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class AutoExpandBuffer<T>(val sectionSize: Int = 128, val paddingSize: Int = sectionSize / 4) : AbstractList<T>() {
	
	override val size get() = bufferSize
	override fun get(index: Int) = buffer[index / sectionSize][index % sectionSize]
	@Synchronized
	fun getSizeAndExpand(): Int {
		while (bufferSize < paddingSize && bufferSize < sourceSize) expandBuffer()
		return bufferSize
	}
	
	@Synchronized
	fun getAndExpand(index: Int): T {
		while (bufferSize <= index + paddingSize && bufferSize < sourceSize) expandBuffer()
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
	private var bufferSize: Int = 0
	@Synchronized
	fun expandBuffer(): Boolean {
		val requireSize = Math.min(sectionSize, sourceSize - bufferSize)
		if (requireSize <= 0) return false
		
		val section = source?.invoke(bufferSize, requireSize) ?: return false
		if (section.size < requireSize) throw RuntimeException("Section size not enough!")
		
		buffer.addLast(section)
		bufferSize += requireSize
		return true
	}
	
	@Synchronized
	fun clearBuffer() {
		buffer.clear()
		bufferSize = 0
	}
	
}

class AutoIndexMap<T> {
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

fun EditText.showSoftInput(forced: Boolean = false) {
	context.inputMethodManager
		.showSoftInput(this,
			if (forced) InputMethodManager.SHOW_FORCED
			else InputMethodManager.SHOW_IMPLICIT
		)
}
