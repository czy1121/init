package me.reezy.cosmo.init.task

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import me.reezy.cosmo.init.log
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class TaskDag private constructor(tasks: List<Task>, private val triggers: Map<String, Task>) {
    companion object {
        @MainThread
        fun launch(tasks: List<Task>, triggers: Set<String> = setOf()): TaskDag {
            return TaskDag(tasks, triggers.associateWith { Task(it) })
        }

    }

    private val done: MutableSet<String> = mutableSetOf()

    private val mainThread = Handler(Looper.getMainLooper())

    private val workerThread = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors().coerceAtLeast(2))

    init {

        // 根据优先级排序
        val sortedTasks = tasks.sortedBy { it.priority }

        // 生成任务映射表
        val map = sortedTasks.associateBy { it.name }

        val delayTasks = mutableListOf<Task>()
        val workerTasks = mutableSetOf<Task>()
        val mainTasks = mutableSetOf<Task>()

        val invalidDepends = tasks.map { it.depends }.flatten().toSet().subtract(map.keys).subtract(triggers.keys)


        sortedTasks.forEach {
            val depends = it.depends.subtract(invalidDepends)
            when {
                // 有依赖的任务
                depends.isNotEmpty() -> {
                    // 检测循环依赖
                    checkDependence(listOf(it.name), depends, map)
                    // 明确任务依赖关系
                    for (taskName in depends) {
                        (triggers[taskName] ?: map[taskName])?.children?.add(it)
                    }
                }
                // 无依赖的任务，延迟执行(main 线程)
                it.delay -> delayTasks.add(it)
                // 无依赖的任务，立即顺序执行(main 线程)
                it.main -> mainTasks.add(it)
                // 无依赖的任务，在 worker 线程执行
                else -> workerTasks.add(it)
            }
        }


        log("===> INVALID DEPENDS : ${invalidDepends.joinToString()}")
        log("===> MAIN TASKS : ${mainTasks.joinToString { it.name } }")
        log("===> DELAY TASKS : ${delayTasks.joinToString { it.name } }")
        log("===> WORKER TASKS : ${workerTasks.joinToString { it.name } }")

        // 无依赖的任务，立即顺序执行(main 线程)
        val time1 = measureTimeMillis {
            for (task in mainTasks) {
                run(task)
            }
        }

        // 无依赖的任务，延迟执行(main 线程)
        val time2 = measureTimeMillis {
            for (task in delayTasks) {
                mainThread.post { run(task) }
            }
        }

        // 无依赖的任务，在 worker 线程执行
        val time3 = measureTimeMillis {
            for (task in workerTasks) {
                workerThread.execute { run(task) }
            }
        }

        log("===> EXECUTE MAIN TASKS : ${time1}ms")
        log("===> DISPATCH DELAY TASKS : ${time2}ms")
        log("===> DISPATCH WORKER TASKS : ${time3}ms")

    }

    fun finish(name: String) {
        triggers[name]?.let {
            finish(it)
        }
    }

    private fun run(task: Task) {
        val delegate = TaskDelegate(this, task)
        val start = System.currentTimeMillis()
        try {
            task.block(delegate)
            log("${if (delegate.pending) "PENDING" else "DONE"} ${task.name}: ${System.currentTimeMillis() - start}ms, ${Thread.currentThread().name}")

            if (!delegate.pending) {
                delegate.finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            log("ERROR ${task.name}: $e")
            delegate.finish()
        }
    }

    internal fun finish(task: Task) = synchronized(done) {
        done.add(task.name)

        task.children.filter { done.containsAll(it.depends) }.forEach {
            if (it.main || it.delay) {
                mainThread.post { run(it) }
            } else {
                workerThread.execute { run(it) }
            }
        }
    }

    private fun checkDependence(path: List<String>, depends: Set<String>, map: Map<String, Task>) {
        depends.forEach {
            if (path.contains(it)) {
                throw RuntimeException("Recycle dependence: $path => $it")
            }
            map[it]?.let { item ->
                checkDependence(path + it, item.depends, map)
            }
        }
    }
}