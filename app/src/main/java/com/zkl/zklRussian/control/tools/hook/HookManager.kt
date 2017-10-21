package com.zkl.zklRussian.control.tools.hook

import android.util.SparseArray
import java.util.concurrent.atomic.AtomicInteger

class HookManager {
	
	private var holdingKey = AtomicInteger(1)
	private val hooks = SparseArray<Hook<*>>()
	
	private fun <T : Any> putHook(hook: Hook<T>): Int{
		val thisKey = holdingKey.getAndAdd(1)
		hooks.put(thisKey, hook)
		return thisKey
	}
	fun <T : Any> putHardHook(value: T): Int =putHook(HardHook(value))
	fun <T : Any> putSoftHook(value: T, restore: () -> T): Int =putHook(SoftHook<T>(value, restore))
	fun removeHook(key: Int) { hooks.remove(key) }
	
	fun getValue(key: Int): Any? = hooks.get(key).value
	fun takeValue(key: Int):Any? {
		val re = getValue(key)
		removeHook(key)
		return re
	}
	
}
