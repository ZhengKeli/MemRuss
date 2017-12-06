package com.zkl.zklRussian.ui

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import com.zkl.zklRussian.R

class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		(getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).nightMode = UiModeManager.MODE_NIGHT_AUTO
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
	
	private fun getShowingFragment(): Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)
	
	
	
	
	private var registeredKey = 0
	private val registeredCallbacks = SparseArray<(granted: Boolean, silent: Boolean) -> Unit>()
	fun requestPermission(permission: String, force: Boolean = false, callback: (granted: Boolean, silent: Boolean) -> Unit) {
		if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED){
			callback(true, true)
		}else {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission) && !force) {
				callback(false, true)
			} else {
				val key = registeredKey++
				ActivityCompat.requestPermissions(this, arrayOf(permission), key)
				registeredCallbacks.put(key, callback)
			}
		}
	}
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		registeredCallbacks.get(requestCode)?.let { callback ->
			registeredCallbacks.remove(requestCode)
			callback(grantResults[0] == PackageManager.PERMISSION_GRANTED, false)
		}
	}
}

interface BackPressedHandler {
	fun onBackPressed(): Boolean
}

val Fragment.mainActivity get() = activity as MainActivity

fun Fragment.jump(fragmentManager: FragmentManager, addToBackStack: Boolean) {
	val transaction = fragmentManager.beginTransaction()
	transaction.replace(R.id.fragment_container, this)
	if (addToBackStack) transaction.addToBackStack(null)
	transaction.commit()
}

fun FragmentManager.popAllBackStack() {
	if (this.backStackEntryCount == 0) return
	val id = this.getBackStackEntryAt(0).id
	this.popBackStack(id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
}
