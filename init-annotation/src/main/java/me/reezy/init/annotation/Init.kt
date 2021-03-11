package me.reezy.init.annotation



@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Init(
        val process: String = "all",        // 指定工作进程名称，main 表示仅在主进程运行，all 表示在所有进程运行
        val background: Boolean = false,    // 是否在工作线程执行任务
        val debugOnly: Boolean = false,     // 是否仅在 DEBUG 模式执行任务
        val compliance: Boolean = false,    // 是否需要合规执行
        val depends: Array<String> = [],    // 依赖的任务列表
        val priority: Short = 0
)