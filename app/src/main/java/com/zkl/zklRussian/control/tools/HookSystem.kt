package com.zkl.zklRussian.control.tools

import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.*

class HookSystem<in KeyType:Any,ValueType:Any> {
	private val references = HashMap<KeyType, Reference<ValueType>>()
	private val keys = HashMap<Reference<ValueType>,KeyType>()
	private val garbageQueue = ReferenceQueue<ValueType>()
	
	@Synchronized operator fun get(key: KeyType): ValueType? {
		clearInvalidHooks()
		return references[key]?.get()
	}
	@Synchronized operator fun set(key: KeyType, value: ValueType?) {
		clearInvalidHooks()
		
		if (value == null) {
			val removedReference = references.remove(key)
			keys.remove(removedReference)
		}else{
			val reference = WeakReference(value, garbageQueue)
			keys.put(reference, key)
			
			val oldReference = references.put(key, reference)
			if (oldReference != null) {
				keys.remove(oldReference)
				oldReference.clear()
			}
		}
	}
	@Synchronized fun remove(key: KeyType){
		clearInvalidHooks()
		val removedReference = references.remove(key)
		keys.remove(removedReference)
	}
	@Synchronized private fun clearInvalidHooks(){
		while (true) {
			val reference=garbageQueue.poll()?:break
			val key = keys[reference]
			references.remove(key)
			keys.remove(reference)
		}
	}
	
}



