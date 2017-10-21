package com.zkl.zklRussian.control

import android.app.Activity
import android.app.Application
import android.support.v4.app.Fragment
import com.zkl.zklRussian.control.note.NotebookShelf
import com.zkl.zklRussian.control.tools.hook.HookManager

class MyApplication : Application() {
	
	//note
	val notebookShelf by lazy { NotebookShelf(workingDir = filesDir.resolve("note")) }
	
	
	//reference
	val hookManager = HookManager()
	
}

val Activity.myApp: MyApplication get() = application as MyApplication
val Fragment.myApp: MyApplication get() = activity.myApp