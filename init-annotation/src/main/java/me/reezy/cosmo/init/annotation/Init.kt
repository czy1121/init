package me.reezy.cosmo.init.annotation


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Init(
    val process: String = "all",        // 指定工作进程名称，main 表示仅在主进程运行，all 表示在所有进程运行
    val leading: Boolean = false,       // 是否前置任务，前置任务全部执行完成后才开始调度其它任务
    val background: Boolean = false,    // 是否在工作线程执行任务
    val manual: Boolean = false,        // 是否需要手动完成
    val debugOnly: Boolean = false,     // 是否仅在 DEBUG 模式执行任务
    val depends: Array<String> = [],    // 依赖的任务列表
    val priority: Short = 0,
)