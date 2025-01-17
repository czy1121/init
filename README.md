# Init
 
Kotlin 实现的 Android 应用初始化任务启动库。

- 支持模块化，按模块加载任务
- 可指定工作进程名称
  - all 表示在所有进程运行
  - main 表示在主进程运行，默认值main
  - 可指定多个进程，逗号分隔 
- 任务默认在工作线程执行
  - `delay = true` 指定任务延迟执行(main 线程)
  - `main = true` 指定任务立即执行(main 线程)
- 可指定任务仅在调试模式执行
- 可指定任务在满足合规条件后执行
- 可指定任务优先级
- 有耗时统计

## 引入依赖

``` groovy
repositories { 
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
} 
dependencies {
    implementation "me.reezy.cosmo:init:0.10.1" 
    ksp "me.reezy.cosmo:init-ksp:0.10.1" 
}
```
 

## 使用

模块配置

```groovy
apply plugin: 'com.google.devtools.ksp'

ksp {
    arg("moduleName", "YOUR_MODULE_NAME")
    arg("packageName", "GENERATED_PACKAGE_NAME")
}
```

简单使用

```kotlin 
InitManager.launch(app, debugMode, generatedPackageName, modules) {
    // 立即执行(main 线程, 通常在 Application.onCreate 中)
    add("main", main = true) {
    }
    // 延迟执行(main 线程, 通常在 Application.onCreate 之后)
    add("delay", delay = true) {
    }
    // 任务默认在工作线程执行
    add("worker") {
    }

    add("task1") {
    }
    add("task2", depends = setOf(InitManager.USER_GRANTED)) {
    }
    add("task3", depends = setOf("task1")) {
    }
}  
 
```
 

通过注解 `@Init` 和 `InitTask` 接口定义一个任务  

```kotlin  
@Init
class OneInit : InitTask {
    override fun execute(app: Application, task: TaskDelegate) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}
``` 

通过注解 `@Init` 的参数配置任务信息

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Init(
  val process: String = "main",       // 指定工作进程名称，main 表示仅在主进程运行，all 表示在所有进程运行
  val delay: Boolean = false,         // 是否延迟执行(main 线程)
  val main: Boolean = false,          // 是否立即顺序执行(main 线程)
  val debugOnly: Boolean = false,     // 是否仅在 DEBUG 模式执行任务
  val depends: Array<String> = [],    // 依赖的任务列表
  val priority: Short = 0,
)
```

ksp会按模块收集任务信息并生成任务加载器(`InitLoader_$moduleName`)以添加任务

```kotlin
class Task(
  val name: String,
  val delay: Boolean = false,         // 是否延迟执行(main 线程)
  val main: Boolean = false,          // 是否立即执行(main 线程)
  val priority: Int = 0,              // 任务运行的优先级，值小的先执行
  val depends: Set<String> = setOf(), // 依赖的任务列表
  val block: (TaskDelegate) -> Unit = {},
) {
  val children: MutableSet<Task> = mutableSetOf()
}
``` 

核心类

- `TaskCollector` 负责收集任务
- `TaskDag` 负责调度任务，支持添加开关任务(没有业务仅作为开关，可手动触发完成，并偿试执行其子任务)
  - 无依赖的任务，在 worker 线程并行执行    
    - `delay == true` 延迟执行(main 线程，通常在 Application.onCreate 之后)
    - `main == true` 立即执行(main 线程，通常在 Application.onCreate 中)，
  - 有依赖的任务，确保无循环依赖，且被依赖的任务先执行 
    - 默认在 worker 线程执行 
    - `main == true` 或 `delay == true` 在 main 线程执行 
- `InitManager` 负责找到各模块的任务加载器并开始启动初始化，它使用了一个合规开关来使相关任务在确定合规后执行
 
 



## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).
