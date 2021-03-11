package me.reezy.init

import android.app.Application
import android.content.Context


object InitManager {

    var compliance: Boolean = false
        set(value) {
            if (value && !field) {
                field = true
                complianceRunnable?.run()
                complianceRunnable = null
            }
        }

    private var complianceRunnable: Runnable? = null

    fun init(app: Application) {
        val modules = app.meta("modules") ?: ""
        init(app, modules.split(",").toTypedArray())
    }

    fun init(app: Application, modules: Array<String>) {

        val trigger = ":compliance"
        val taskList = TaskList(app)
        val manager = TaskManager(taskList, setOf(trigger))

        modules.map { it.replace("[^0-9a-zA-Z_]+".toRegex(), "") }.forEachIndexed { index, it ->
            try {
                val loaderClass = Class.forName("$PKG.generated.InitLoader_$it")
                val loader = loaderClass.newInstance()
                loaderClass.getMethod("load", TaskList::class.java, Int::class.java).invoke(loader, taskList, index)
            } catch (e: ClassNotFoundException) {
                log("There is no Loader in module: $it.")
            } catch (e: Throwable) {
                log(e.message!!)
                e.printStackTrace()
            }
        }

        val sp = app.getSharedPreferences("${app.packageName}-$trigger", Context.MODE_PRIVATE)
        compliance = sp.getBoolean(trigger, false)
        if (compliance) {
            manager.finish(trigger)
        } else {
            complianceRunnable = Runnable {
                manager.finish(trigger)
                sp.edit().putBoolean(trigger, true).apply()
            }
        }

        manager.start()
    }
}