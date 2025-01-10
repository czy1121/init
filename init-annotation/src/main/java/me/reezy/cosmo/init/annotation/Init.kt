package me.reezy.cosmo.init.annotation


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Init(
    val process: String = "main",       // 指定工作进程名称，main 表示仅在主进程运行，all 表示在所有进程运行
    val delay: Boolean = false,         // 是否延迟执行(main 线程)
    val main: Boolean = false,          // 是否立即执行(main 线程)
    val debugOnly: Boolean = false,     // 是否仅在 DEBUG 模式执行任务
    val depends: Array<String> = [],    // 依赖的任务列表
    val priority: Short = 0,
)