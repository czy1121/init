package me.reezy.cosmo.init.task

import me.reezy.cosmo.init.log


class TaskCollector(
    private val packageName: String,
    private val currentProcessName: String,
    private val isDebuggable: Boolean,
) {
    private val mItems: MutableList<Task> = mutableListOf()

    val items: List<Task> = mItems

    fun add(
        name: String,
        process: String = "main",
        delay: Boolean = false,
        main: Boolean = false,
        debugOnly: Boolean = false,
        priority: Int = 0,
        depends: Set<String> = setOf(),
        block: (TaskDelegate) -> Unit,
    ) {
        if (debugOnly && !isDebuggable) {
            log("SKIPPED $name : debug only")
            return
        }
        if (process != "all") {
            val processes = process.split(",").map {
                when {
                    it == "main" -> packageName
                    it.contains(":") -> it
                    else -> "$packageName:$it"
                }
            }
            if (!processes.contains(currentProcessName)) {
                log("SKIPPED $name : require process [$process]")
                return
            }
        }

        mItems.add(Task(name, delay, main, priority, depends, block))
    }
}