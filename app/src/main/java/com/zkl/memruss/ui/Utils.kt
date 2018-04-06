package com.zkl.memruss.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.SparseArray
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.zkl.memruss.control.note.NotebookKey
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.inputMethodManager
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

//bundle extensions
internal operator fun Bundle?.plus(bundle: Bundle): Bundle = (this ?: Bundle()).apply { putAll(bundle) }

// fragment arguments
private const val argName_notebookKey = "notebookKey"
private const val argName_noteId = "noteId"
var Fragment.argNotebookKey: NotebookKey by object : ReadWriteProperty<Fragment, NotebookKey> {
	
	override fun getValue(thisRef: Fragment, property: KProperty<*>): NotebookKey {
		return thisRef.arguments.getSerializable(argName_notebookKey) as NotebookKey
	}
	
	override fun setValue(thisRef: Fragment, property: KProperty<*>, value: NotebookKey) {
		thisRef.arguments += bundleOf(argName_notebookKey to value)
	}
	
}
var Fragment.argNoteId: Long by object : ReadWriteProperty<Fragment, Long> {
	
	override fun getValue(thisRef: Fragment, property: KProperty<*>): Long {
		return thisRef.arguments.getLong(argName_noteId)
	}
	
	override fun setValue(thisRef: Fragment, property: KProperty<*>, value: Long) {
		thisRef.arguments += bundleOf(argName_noteId to value)
	}
}

fun <T : Fragment> Class<T>.newInstance(notebookKey: NotebookKey): T {
	return newInstance().apply { argNotebookKey = notebookKey }
}

fun <T : Fragment> Class<T>.newInstance(notebookKey: NotebookKey, noteId: Long): T {
	return newInstance(notebookKey).apply { argNoteId = noteId }
}