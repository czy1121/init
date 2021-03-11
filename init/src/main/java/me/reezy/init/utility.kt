package me.reezy.init

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log


internal const val PKG = "me.reezy.init"

internal fun log(message: String) {
    Log.d(PKG, message);
}

internal fun Context.meta(key: String): String? {
    try {
        return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData?.getString(key)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return null
}

internal fun Context.isDebuggable(): Boolean = try {
    applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
} catch (e: Exception) {
    e.printStackTrace()
    false
}

internal fun Context.resolveCurrentProcessName(): String? {
    val pid = Process.myPid()
    (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses?.forEach {
        if (it.pid == pid) {
            return it.processName
        }
    }
    return null
}