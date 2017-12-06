package com.zkl.zklRussian.ui

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
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
	
	
	//fragment access
	private fun getShowingFragment(): Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)
	
	
	//backPressed dispatch
	interface BackPressedHandler {
		fun onBackPressed(): Boolean
	}
	override fun onBackPressed() {
		val backHandler = getShowingFragment() as? BackPressedHandler
		val absorbed = backHandler?.onBackPressed() == true
		if (!absorbed) super.onBackPressed()
	}
	
	
	//permissions request
	private val requestPermissionCallbacks = AutoIndexMap<RequestPermissionCallback>()
	fun requestPermission(permission: String, force: Boolean = false, callback: RequestPermissionCallback) {
		if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED){
			callback(true, true)
		}else {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission) && !force) {
				callback(false, true)
			} else {
				val key = requestPermissionCallbacks.put(callback)
				ActivityCompat.requestPermissions(this, arrayOf(permission), key)
			}
		}
	}
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		requestPermissionCallbacks.remove(requestCode)?.invoke(
			grantResults[0] == PackageManager.PERMISSION_GRANTED, false)
	}
	
}


//self access
val Fragment.mainActivity get() = activity as MainActivity


//fragment jumping
fun Fragment.jump(fragmentManager: FragmentManager, addToBackStack: Boolean) {
	val transaction = fragmentManager.beginTransaction()
	transaction.replace(R.id.fragment_container, this)
	if (addToBackStack) transaction.addToBackStack(null)
	transaction.commit()
}
fun DialogFragment.show(fragmentManager: FragmentManager){
	show(fragmentManager,null)
}


//permission request
typealias RequestPermissionCallback = (granted: Boolean, silent: Boolean) -> Unit
