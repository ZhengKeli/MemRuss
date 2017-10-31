package com.zkl.zklRussian.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import com.zkl.zklRussian.R

class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_notebook)
		
		jumpToFragment(NotebookShelfFragment().also {
			it.autoJumpToFirst = true
		},false)
		
	}
	
	//fragment jump
	fun jumpToFragment(fragment: Fragment, addToBackStack:Boolean) {
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

val Fragment.mainActivity get() = activity as MainActivity
fun FragmentTransaction.replace(fragment: Fragment) = replace(R.id.fragment_container,fragment)!!

