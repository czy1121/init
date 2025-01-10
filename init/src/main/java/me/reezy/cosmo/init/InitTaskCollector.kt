package me.reezy.cosmo.init

import android.app.Application
import me.reezy.cosmo.init.task.TaskCollector

class InitTaskCollector(
    private val collector: TaskCollector,
    private val app: Application,
    private val moduleName: String,
    private val moduleIndex: Int,
) {
    fun add(
        clazz: Class<out InitTask>,
        name: String,
        process: String,
        delay: Boolean,
        main: Boolean,
        debugOnly: Boolean,
        priority: Short,
        depends: Set<String>,
    ) {
        val fullName = "$moduleName:$name"
        val realPriority = (moduleIndex shl 16) or (priority.toInt() + Short.MAX_VALUE)
        collector.add(fullName, process, delay, main, debugOnly, realPriority, depends) {
            clazz.newInstance().execute(app, it)
        }
    }
}