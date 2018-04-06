package com.zkl.memruss.utils

inline fun <T> tryOrNull(body: () -> T): T? {
	return try {
		body()
	} catch (e: Exception) {
		e.printStackTrace()
		null
	}
}