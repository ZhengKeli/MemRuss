package com.zkl.zklRussian.control.tools.hook

import java.lang.ref.SoftReference

interface Hook<out T : Any> {
	val value: T
}

class HardHook<out T : Any>(override val value: T) : Hook<T>

class SoftHook<out T : Any>(value: T, private val onRestore: () -> T) : Hook<T> {
	private var reference = SoftReference(value)
	override val value: T get() = reference.get() ?: onRestore().also { reference = SoftReference(it) }
}
