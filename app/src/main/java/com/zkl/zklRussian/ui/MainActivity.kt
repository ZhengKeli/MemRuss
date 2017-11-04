package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.zkl.zklRussian.R

class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_notebook)
		
		if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
			supportFragmentManager.jumpTo(NotebookShelfFragment.newInstance(true))
		}
	}
	
}

fun FragmentManager.jumpTo(fragment: Fragment, addToBackStack:Boolean=false) {
	val transaction=beginTransaction()
	transaction.replace(R.id.fragment_container, fragment)
	if(addToBackStack) transaction.addToBackStack(null)
	transaction.commit()
}
fun FragmentManager.popAllBackStack(){
	if(this.backStackEntryCount==0) return
	val id = this.getBackStackEntryAt(0).id
	this.popBackStack(id,FragmentManager.POP_BACK_STACK_INCLUSIVE)
}
