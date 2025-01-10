package com.demo.app

import android.app.Application
import android.util.Log
import me.reezy.cosmo.init.InitTask
import me.reezy.cosmo.init.annotation.Init
import me.reezy.cosmo.init.task.TaskDelegate

const val TAG = "OoO.init"

@Init(priority = 20)
class AlphaInit : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}


@Init(main = true, process = "main", priority = 20)
class BetaInit : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}


@Init(main = true, process = "main", priority = 10)
class GammaInit : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}


@Init(priority = 40)
class DeltaInit : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}

@Init(priority = 10, depends = ["DeltaInit", "OneInit", "abc"])
class FourInit : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}
@Init(priority = 10)
class OneInit : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}


@Init(priority = 10, main = true)
class TwoInit : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}

@Init
class ThreeInit : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}



@Init(main = true)
class Alone1 : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} started")
        Thread.sleep(5000)
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} done")
    }
}

@Init(main = true)
class Alone2 : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} started")
        Thread.sleep(5000)
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} done")
    }
}
@Init(main = true)
class Alone3 : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} started")
        Thread.sleep(5000)
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} done")
    }
}
@Init(main = true)
class Alone4 : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} started")
        Thread.sleep(5000)
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} done")
    }
}
@Init(main = true)
class Alone5 : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} started")
        Thread.sleep(5000)
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} done")
    }
}
@Init(main = true)
class Alone6 : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} started")
        Thread.sleep(5000)
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name} done")
    }
}