package com.zkl.memruss.control.tools

inline fun <T> silence(action: () -> T): T? {
	return try {
		action()
	} catch (e: Exception) {
		null
	}
}
