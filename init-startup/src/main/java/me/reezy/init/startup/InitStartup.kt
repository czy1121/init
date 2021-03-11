package me.reezy.init.startup

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import me.reezy.init.InitManager
import java.lang.Exception

class InitStartup : Initializer<Unit> {
    override fun create(context: Context) {
        application?.let { app ->
            InitManager.init(app)
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

}