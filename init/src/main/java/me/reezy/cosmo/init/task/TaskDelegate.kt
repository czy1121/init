package me.reezy.cosmo.init.task

class TaskDelegate(private val dag: TaskDag, private val task: Task) {


    var pending: Boolean = false

    fun finish() {
        dag.finish(task)
    }
}