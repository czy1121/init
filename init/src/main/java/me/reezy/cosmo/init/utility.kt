package me.reezy.cosmo.init

import android.util.Log


internal var currentProcessName: String = ""
internal var isDebugMode: Boolean = false

internal fun log(message: String) {
    if (isDebugMode) {
        Log.d("OoO.init", "[$currentProcessName] $message");
    }
}