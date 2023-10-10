package me.reezy.cosmo.init

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import me.reezy.cosmo.init.dag.TaskDag
import me.reezy.cosmo.init.dag.TaskList
import java.io.File

object InitManager {
    const val USER_GRANTED = ":user-granted"

    var isUserGranted: Boolean = false
        set(value) {
            if (value && !field) {
                field = true
                onUserGranted?.run()
                onUserGranted = null

            }
        }

    private var onUserGranted: Runnable? = null

    fun launch(app: Application, debugMode: Boolean, generatedPackageName: String = app.packageName, modules: List<String> = listOf(), block: TaskList.() -> Unit = {}) {

        val processName = app.resolveCurrentProcessName() ?: ""

        val tasks = TaskList(app.packageName, processName, debugMode).apply(block).collect(app, generatedPackageName, modules).items

        val dag = TaskDag.launch(tasks, setOf(USER_GRANTED))


        val file = File(app.filesDir, "init-user-granted")

        isUserGranted = file.exists()

        if (isUserGranted) {
            dag.finish(USER_GRANTED)
        } else {
            onUserGranted = Runnable {
                dag.finish(USER_GRANTED)

                file.createNewFile()
            }
        }
    }

    private fun TaskList.collect(app: Application, generatedPackageName: String, modules: List<String>): TaskList {
        modules.map { it.replace("[^0-9a-zA-Z_]+".toRegex(), "") }.forEachIndexed { index, it ->
            try {
                val loaderClass = Class.forName("$generatedPackageName.generated.InitLoader_$it")
                val loaderMethod = loaderClass.getMethod("load", InitTaskCollector::class.java)

                val collector = InitTaskCollector(app, this, it, index)
                loaderMethod.invoke(loaderClass.newInstance(), collector)
            } catch (e: ClassNotFoundException) {
                TaskDag.log("There is no Loader in module: $it.")
            } catch (e: Throwable) {
                TaskDag.log(e.message!!)
                e.printStackTrace()
            }
        }
        return this
    }

    private fun Context.resolveCurrentProcessName(): String? {
        val pid = Process.myPid()
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses?.forEach {
            if (it.pid == pid) {
                return it.processName
            }
        }
        return null
    }
}