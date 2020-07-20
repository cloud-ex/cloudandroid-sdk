package com.sl.ui.library.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.sl.ui.library.data.PermissionResult
import com.sl.ui.library.ui.fragment.LiveFragment

class LivePermissions {
    companion object {
        const val TAG = "permissions"
    }

    constructor(activity: AppCompatActivity) {
        liveFragment = getInstance(activity.supportFragmentManager)
    }

    constructor(fragment: Fragment) {
        liveFragment = getInstance(fragment.childFragmentManager)
    }

    @Volatile
    private var liveFragment: LiveFragment? = null

    private fun getInstance(fragmentManager: FragmentManager) =
        liveFragment ?: synchronized(this) {
            liveFragment ?: if (fragmentManager.findFragmentByTag(TAG) == null) LiveFragment().run {
                fragmentManager.beginTransaction().add(this, TAG).commitNow()
                this
            } else fragmentManager.findFragmentByTag(TAG) as LiveFragment
        }

    @RequiresApi(Build.VERSION_CODES.M)
    fun request(vararg permissions: String): MutableLiveData<PermissionResult> {
        return this.requestArray(permissions)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestArray(permissions: Array<out String>): MutableLiveData<PermissionResult> {
        liveFragment!!.requestPermissions(permissions)
        return liveFragment!!.liveData
    }
}