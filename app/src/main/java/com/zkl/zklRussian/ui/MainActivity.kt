package com.zkl.zklRussian.ui

import android.app.UiModeManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.zkl.zklRussian.R

class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		(getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).nightMode=UiModeManager.MODE_NIGHT_AUTO
		setContentView(R.layout.activity_main)
		
		if (getShowingFragment() == null) {
			NotebookShelfFragment.newInstance(true).jump(supportFragmentManager, false)
		}
	}
	
	override fun onBackPressed() {
		val backHandler = getShowingFragment() as? BackPressedHandler
		val absorbed = backHandler?.onBackPressed() == true
		if (!absorbed) super.onBackPressed()
	}
	
	private fun getShowingFragment():Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)
}

interface BackPressedHandler{
	fun onBackPressed():Boolean
}

val Fragment.mainActivity get() = activity as MainActivity
fun Fragment.jump(fragmentManager: FragmentManager, addToBackStack: Boolean) {
	val transaction= fragmentManager.beginTransaction()
	transaction.replace(R.id.fragment_container, this)
	if(addToBackStack) transaction.addToBackStack(null)
	transaction.commit()
}
fun FragmentManager.popAllBackStack(){
	if(this.backStackEntryCount==0) return
	val id = this.getBackStackEntryAt(0).id
	this.popBackStack(id,FragmentManager.POP_BACK_STACK_INCLUSIVE)
}
