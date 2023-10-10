package me.reezy.cosmo.init.dag

import android.util.Log
import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.measureTimeMillis

class TaskDag private constructor(tasks: List<Task>, private val triggers: Map<String, Task>) {

    companion object {
        private val doNothing = object : TaskInterface {
            override fun finish() {
            }
        }

        var log: (String) -> Unit = {
            Log.d("OoO.TaskDag", it);
        }

        @MainThread
        fun launch(tasks: List<Task>, triggers: Set<String> = setOf()): TaskDag {
            return TaskDag(tasks, triggers.associateWith { Task(it) })
        }

        @MainThread
        fun launch(
            packageName: String,
            currentProcessName: String,
            isDebuggable: Boolean = false,
            triggers: Set<String> = setOf(),
            modules: List<String> = listOf(),
            block: TaskList.() -> Unit = {}
        ): TaskDag {
            val taskList = TaskList(packageName, currentProcessName, isDebuggable)

            taskList.apply(block)

            modules.map { it.replace("[^0-9a-zA-Z_]+".toRegex(), "") }.forEachIndexed { index, it ->
                try {
                    val loaderClass = Class.forName("me.reezy.cosmo.init.generated.InitLoader_$it")
                    val loader = loaderClass.newInstance()
                    loaderClass.getMethod("load", TaskList::class.java, Int::class.java).invoke(loader, taskList, index)
                } catch (e: ClassNotFoundException) {
                    log("There is no Loader in module: $it.")
                } catch (e: Throwable) {
                    log(e.message!!)
                    e.printStackTrace()
                }
            }

            return TaskDag(taskList.items, triggers.associateWith { Task(it) }, )
        }
    }

    private val done: MutableSet<String> = mutableSetOf()

    private val scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)

    init {
        // 根据优先级排序
        val sortedTasks = tasks.sortedBy { it.priority }

        // 生成任务映射表
        val map = sortedTasks.associateBy { it.name }

        val leadTasks = mutableSetOf<Task>()
        val syncTasks = mutableSetOf<Task>()
        val aloneTasks = mutableSetOf<Task>()

        sortedTasks.forEach {
            when {
                // 前置任务
                it.leading -> leadTasks.add(it)
                // 有依赖的任务
                it.depends.isNotEmpty() -> {
                    // 检测循环依赖
                    checkDependence(listOf(it.name), it.depends, map)
                    // 明确任务依赖关系
                    it.depends.forEach { taskName ->
                        val item = triggers[taskName] ?: map[taskName] ?: throw Throwable("Cannot find dependence $taskName ")
                        item.children.add(it)
                    }
                }
                // 无依赖的异步任务
                it.background -> aloneTasks.add(it)
                // 无依赖的同步任务，在主线程执行
                else -> syncTasks.add(it)
            }
        }


        val time = measureTimeMillis {
            // 前置任务全部执行完成后才开始调度其它任务
            val time1 = measureTimeMillis {
                leadTasks.forEach(this::execute)
            }
            log("=======> EXECUTE LEADING TASKS : ${time1}ms")

            // 无依赖的异步任务，在子线程并行执行
            val time2 = measureTimeMillis {
                flowOf(*aloneTasks.toTypedArray()).onEach(this::execute).launchIn(scope)
            }
            log("=======> DISPATCH ALONE TASKS : ${time2}ms")

            // 无依赖的同步任务，在主线程顺序执行
            val time3 = measureTimeMillis {
                syncTasks.forEach(this::execute)
            }
            log("=======> EXECUTE SYNC TASKS : ${time3}ms")
        }
        log("=======> LAUNCHED : ${time}ms")
    }

    fun finish(name: String) {
        triggers[name]?.let {
            finish(name, it.children)
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



    private fun execute(task: Task) {

        val start = System.currentTimeMillis()

        try {
            task.block(if (!task.manual) doNothing else object : TaskInterface {
                override fun finish() {
                    finish(task, start)
                }
            })
            if (!task.manual) {
                finish(task, start)
            }
        } catch (e: Exception) {
            log("===> ${task.name} ERROR : $e")
            e.printStackTrace()
            finish(task, start)
        }
    }

    private fun finish(task: Task, start: Long) {
        val time = System.currentTimeMillis() - start
        log("===> ${task.name} DONE : ${time}ms, ${Thread.currentThread().name}")
        finish(task.name, task.children)
    }


    private fun finish(name: String, children: MutableSet<Task>) = synchronized(done) {
        done.add(name)
        children.filter { done.containsAll(it.depends) }.forEach {
            val dispatcher = if (it.background) Dispatchers.Default else Dispatchers.Main
            scope.launch(dispatcher) {
                execute(it)
            }
        }
    }
}