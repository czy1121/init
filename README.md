# Init
 
Kotlin + Flow 实现的 Android 应用初始化任务启动库。

- 支持模块化，按模块加载任务
- 可指定工作进程名称，main 表示仅在主进程运行，all 表示在所有进程运行，默认值all
- 可指定任务仅在工作线程执行
- 可指定任务仅在调试模式执行
- 可指定任务在满足合规条件后执行
- 可指定任务优先级，决定同模块内无依赖同步任务的执行顺序
- 可指定依赖任务列表，能检测循环依赖
- 使用 Flow 调度任务 
- 有耗时统计

## 引入依赖

``` groovy
repositories { 
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
} 
dependencies {
    implementation "me.reezy.cosmo:init:0.8.0" 
    ksp "me.reezy.cosmo:init-ksp:0.8.0" 

    // 使用 init-startup 代替 init 可以利用 Jetpack Startup 库自动初始化
    // 无需在 Application.onCreate 调用 InitManager.init()
    implementation "me.reezy.cosmo:init-startup:0.8.0" 
}
```
 

## 使用

在 `AndroidManifest.xml` 的 `<application>` 里添加模块

```xml 
<meta-data android:name="modules" android:value="app" />
``` 

通过注解 `@Init` 和 `InitTask` 接口定义一个任务  

```kotlin  
@Init
class OneInit : InitTask {
    override fun execute(app: Application) {
        Log.e(TAG, "this is ${javaClass.simpleName} in ${Thread.currentThread().name}")
    }
}
``` 

通过注解 `@Init` 的参数配置任务信息

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Init(
    val process: String = "all",        // 指定工作进程名称，main 表示仅在主进程运行，all 表示在所有进程运行
    val leading: Boolean = false,       // 是否前置任务，前置任务全部执行完成后才开始调度其它任务
    val background: Boolean = false,    // 是否在工作线程执行任务
    val manual: Boolean = false,        // 是否需要手动完成
    val debugOnly: Boolean = false,     // 是否仅在 DEBUG 模式执行任务
    val compliance: Boolean = false,    // 是否需要合规执行
    val depends: Array<String> = [],    // 依赖的任务列表
    val priority: Short = 0,
)
```

ksp会按模块收集任务信息并生成任务加载器(`InitLoader_$moduleName`)，任务加载器用于添加任务到`TaskList`

```kotlin
class Task(
    val name: String,
    val leading: Boolean = false,       // 是否前置任务，前置任务全部执行完成后才开始调度其它任务
    val background: Boolean = false,    // 是否在工作线程执行任务
    val manual: Boolean = false,        // 是否需要手动完成
    val priority: Int = 0,              // 任务运行的优先级，值小的先执行
    val depends: Set<String> = setOf(), // 依赖的任务列表
    val block: (TaskInterface) -> Unit = {},
) {
    val children: MutableSet<Task> = mutableSetOf() // 子任务列表
}
``` 

核心类

- `TaskList` 负责持有和添加任务
- `TaskDag` 负责调度任务，支持添加开关任务(没有业务仅作为开关，可手动触发完成，并偿试执行其子任务)
  - 无依赖的异步任务，在子线程并行执行
  - 无依赖的同步任务，在主线程顺序执行
  - 有依赖的任务，确保无循环依赖，且被依赖的任务先执行
- `InitManager` 负责找到各模块的任务加载器并开始启动初始化，它使用了一个合规开关来使相关任务在确定合规后执行
 
 

```kotlin

InitManager.launch(app, debugMode) {
  add("task1") {
  }
  add("task2", depends = setOf(InitManager.COMPLIANCE)) {
  }
  add("task3", depends = setOf("task1")) {
  }
}  
 
```


## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).
