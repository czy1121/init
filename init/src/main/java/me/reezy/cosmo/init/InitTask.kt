package me.reezy.cosmo.init

import android.app.Application
import me.reezy.cosmo.init.dag.TaskInterface

interface InitTask {
    fun execute(app: Application, task: TaskInterface)
}