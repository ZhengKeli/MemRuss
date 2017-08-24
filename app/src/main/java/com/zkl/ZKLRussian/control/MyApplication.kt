package com.zkl.ZKLRussian.control

import android.app.Activity
import android.app.Application
import android.support.v4.app.Fragment
import com.zkl.ZKLRussian.control.note.NoteManager

class MyApplication : Application() {
	
	//note
	val noteManager by lazy { NoteManager(workingDir = filesDir.resolve("note")) }
	
}

val Activity.myApp: MyApplication get() = application as MyApplication
val Fragment.myApp:MyApplication get() = activity.myApp