package me.reezy.cosmo.startup

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.startup.Initializer
import me.reezy.cosmo.init.InitManager
import java.lang.Exception

class InitStartup : Initializer<Unit> {
    override fun create(context: Context) {
        val modules = context.meta("modules")?.split(",") ?: listOf()
        application?.let { app ->
            InitManager.launch(app, false, modules = modules)
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }

    private val application: Application?
        @SuppressLint("PrivateApi")
        get() = try {
            Class.forName("android.app.ActivityThread")?.getMethod("currentApplication")
                ?.invoke(null) as? Application
        } catch (ex: Exception) {
            null
        }

    private fun Context.meta(key: String): String? {
        try {
            return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getString(key)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }
}