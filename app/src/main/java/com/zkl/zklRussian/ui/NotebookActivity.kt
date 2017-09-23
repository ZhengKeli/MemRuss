package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import com.zkl.zklRussian.R
import com.zkl.zklRussian.control.myApp

class NotebookActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_notebook)
		
		val bookSummaries = myApp.noteManager.loadBookSummaries()
		if (bookSummaries.isEmpty()) {
			jumpToFragment(NotebookInfantFragment())
		} else {
			val firstSummary=bookSummaries.first()
			val firstNotebook=myApp.noteManager.openMutableNotebook(firstSummary.file)
			jumpToFragment(NotebookFragment(myApp.noteManager.registerNotebook(firstNotebook)))
		}
		
	}
	
	//fragment jump
	fun jumpToFragment(fragment: Fragment, addToBackStack:Boolean=false) {
		if (addToBackStack) {
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.addToBackStack(null)
				.commit()
		}else{
			clearBackStack()
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.commit()
		}
	}
	fun jumpBackFragment(){
		supportFragmentManager.popBackStack()
	}
	fun clearBackStack(){
		if(supportFragmentManager.backStackEntryCount==0) return
		val id = supportFragmentManager.getBackStackEntryAt(0).id
		supportFragmentManager.popBackStack(id,FragmentManager.POP_BACK_STACK_INCLUSIVE)
	}
	
}

val Fragment.notebookActivity get() = activity as NotebookActivity
fun FragmentTransaction.replace(fragment: Fragment) = replace(R.id.fragment_container,fragment)!!

