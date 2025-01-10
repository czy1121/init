package me.reezy.cosmo.init

import android.app.Application
import me.reezy.cosmo.init.task.TaskDelegate

interface InitTask {
    fun execute(app: Application, task: TaskDelegate)
}