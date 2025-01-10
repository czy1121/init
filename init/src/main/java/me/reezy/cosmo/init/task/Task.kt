package me.reezy.cosmo.init.task

class Task(
    val name: String,
    val delay: Boolean = false,         // 是否延迟执行(main 线程)
    val main: Boolean = false,          // 是否立即执行(main 线程)
    val priority: Int = 0,              // 任务运行的优先级，值小的先执行
    val depends: Set<String> = setOf(), // 依赖的任务列表
    val block: (TaskDelegate) -> Unit = {},
) {
    val children: MutableSet<Task> = mutableSetOf()
}