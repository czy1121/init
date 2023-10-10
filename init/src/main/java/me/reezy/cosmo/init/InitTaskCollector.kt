package me.reezy.cosmo.init

import android.app.Application
import me.reezy.cosmo.init.dag.TaskList

class InitTaskCollector(
    private val app: Application,
    private val taskList: TaskList,
    private val moduleName: String,
    private val moduleIndex: Int,
) {
    fun add(
        clazz: Class<out InitTask>, process: String, leading: Boolean, background: Boolean,
        manual: Boolean, debugOnly: Boolean, priority: Short, depends: Set<String>,
    ) {
        val name = "$moduleName:${clazz.simpleName}"
        val realPriority = (moduleIndex shl 16) or (priority.toInt() + Short.MAX_VALUE)
        taskList.add(name, process, leading, background, manual, debugOnly, realPriority, depends) {
            clazz.newInstance().execute(app, it)
        }
    }
}