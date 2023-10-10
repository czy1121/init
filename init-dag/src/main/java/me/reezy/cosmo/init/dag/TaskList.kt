package me.reezy.cosmo.init.dag


class TaskList(
    private val packageName: String,
    private val currentProcessName: String,
    private val isDebuggable: Boolean,
) {
    private val mItems: MutableList<Task> = mutableListOf()

    val items: List<Task> = mItems

    fun add(name: String, process: String = "all", leading: Boolean = false, background: Boolean = false, manual: Boolean = false, debugOnly: Boolean = false, priority: Int = 0, depends: Set<String> = setOf(), block: (TaskInterface) -> Unit) {
        if (debugOnly && !isDebuggable) {
            TaskDag.log("===> $name SKIPPED : debug only")
            return
        }
        when (process) {
            "all" -> {}
            "main" -> {
                if (currentProcessName != packageName) {
                    TaskDag.log("===> $name SKIPPED : main process only")
                    return
                }
            }
            else -> {
                if (currentProcessName != "${packageName}:${process}") {
                    TaskDag.log("===> $name SKIPPED : process [$currentProcessName] [${packageName}:${process}] only")
                    return
                }
            }
        }
        mItems.add(Task(name, leading, background, manual, priority, depends, block))
    }
}