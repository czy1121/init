package me.reezy.init

import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

class TaskManager(private val taskList: TaskList, triggers: Set<String> = setOf()) {

    private val done: MutableSet<String> = mutableSetOf()
    private val triggerMap: Map<String, Task> = triggers.map { it to Task(it) }.toMap()

    fun start() {
        // 根据优先级排序
        taskList.items.sortBy { it.priority }

        // 生成任务映射表
        val map = taskList.items.map { it.name to it }.toMap()


        val syncTasks = mutableSetOf<Task>()
        val aloneTasks = mutableSetOf<Task>()

        taskList.items.forEach {
            when {
                // 有依赖的任务
                it.depends.isNotEmpty() -> {
                    // 检测循环依赖
                    checkDependence(listOf(it.name), it.depends, map)
                    // 明确任务依赖关系
                    it.depends.forEach { taskName ->
                        val item = triggerMap[taskName] ?: map[taskName] ?: throw Throwable("Cannot find dependence $taskName ")
                        item.children.add(it)
                    }
                }
                // 无依赖的异步任务
                it.background -> aloneTasks.add(it)
                // 无依赖的同步任务，在主线程执行
                else -> syncTasks.add(it)
            }
        }

        // 无依赖的异步任务，在子线程并行执行
        aloneTasks.asFlow().onEach(this::execute).launchIn(GlobalScope)

        // 无依赖的同步任务，在主线程顺序执行
        if (Looper.getMainLooper() === Looper.myLooper()) {
            syncTasks.forEach(this::execute)
        } else {
            syncTasks.asFlow().flowOn(Dispatchers.Main).onEach(this::execute).launchIn(GlobalScope)
        }
    }

    private fun checkDependence(path: List<String>, depends: Set<String>, map: Map<String, Task>) {

        depends.forEach {
            if (path.contains(it)) {
                throw Throwable("Recycle dependence: $path => $it")
            }
            map[it]?.let { item ->
                checkDependence(path + it, item.depends, map)
            }
        }
    }

    fun finish(name: String) {
        triggerMap[name]?.let {
            finish(name, it.children)
        }
    }

    private fun execute(task: Task) {

        val time = measureTimeMillis {
            try {
                task.block()
            } catch (e: Exception) {
                log("===> ${task.name} ERROR : $e")
                e.printStackTrace()
            }
        }
        log("===> ${task.name} DONE : ${time}ms")

        finish(task.name, task.children)
    }


    private fun finish(name: String, children: MutableSet<Task>) = synchronized(done) {
        done.add(name)
        children.filter { done.containsAll(it.depends) }.forEach {
            val flowOn = if (it.background) Dispatchers.Default else Dispatchers.Main
            flowOf(it).flowOn(flowOn).onEach(this::execute).launchIn(GlobalScope)
        }
    }
}