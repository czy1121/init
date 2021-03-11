package me.reezy.init

class Task(
    val name: String,
    val background: Boolean = false,    // 是否在工作线程执行任务
    val priority: Int = 0,              // 进程运行的优先级，值小的先执行
    val depends: Set<String> = setOf(), // 依赖的任务列表
    val block: () -> Unit = {},
) {
    val children: MutableSet<Task> = mutableSetOf()
}