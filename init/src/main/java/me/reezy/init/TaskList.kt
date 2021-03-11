package me.reezy.init

import android.app.Application

class TaskList(private val app: Application) {
    private val isDebuggable = app.isDebuggable()
    private val currentProcessName = app.resolveCurrentProcessName()

    internal val items: ArrayList<Task> = ArrayList()

    fun add(moduleName: String, moduleIndex: Int, clazz: Class<out InitTask>, process: String, background: Boolean, debugOnly: Boolean, priority: Short, depends: Set<String>) {
        val name = "$moduleName:${clazz.simpleName}"
        val realPriority = (moduleIndex shl 16) or (priority.toInt() + Short.MAX_VALUE)
        add(name, process, background, debugOnly, realPriority, depends) {
            clazz.newInstance().execute(app)
        }
    }

    fun add(name: String, process: String = "all", background: Boolean = false, debugOnly: Boolean = false, priority: Int = 0, depends: Set<String> = setOf(), block: () -> Unit) {
        if (debugOnly && !isDebuggable) {
            log("===> $name SKIPPED : debug only")
            return
        }
        when(process) {
            "all" -> { }
            "main" -> {
                if (currentProcessName != app.packageName) {
                    log("===> $name SKIPPED : main process only")
                    return
                }
            }
            else -> {
                if (currentProcessName != "${app.packageName}:${process}") {
                    log("===> $name SKIPPED : process [$currentProcessName] [${app.packageName}:${process}] only")
                    return
                }
            }
        }
        items.add(Task(name, background, priority, depends, block))
    }
}