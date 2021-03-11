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
- 仅200多行代码，简单明了
- 有耗时统计

## 引入依赖

``` groovy
repositories { 
    maven { url "https://gitee.com/ezy/repo/raw/android_public/"}
} 
dependencies {
    implementation "me.reezy.init:init:0.9.0" 
    kapt "me.reezy.init:init-compiler:0.9.0" 

    // 使用 init-startup 代替 init 可以利用 Jetpack Startup 库自动初始化
    // 无需在 Application.onCreate 调用 InitManager.init()
    implementation "me.reezy.init:init-startup:0.9.0" 
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
    val background: Boolean = false,    // 是否在工作线程执行任务
    val debugOnly: Boolean = false,     // 是否仅在 DEBUG 模式执行任务
    val compliance: Boolean = false,    // 是否需要合规执行
    val depends: Array<String> = [],    // 依赖的任务列表
    val priority: Short = 0             // 
)
```

APT会按模块收集任务信息并生成任务加载器(`InitLoader_$moduleName`)，任务加载器用于添加任务到`TaskList`

```kotlin
class Task(
    val name: String,                   // APT收集的任务名称格式为 "$moduleName:${clazz.simpleName}"
    val background: Boolean = false,    // 是否在工作线程执行任务
    val priority: Int = 0,              // 进程运行的优先级，值小的先执行
    val depends: Set<String> = setOf(), // 依赖的任务列表，同模块只需指定"${clazz.simpleName}"，跨模块需要指定 "$moduleName:${clazz.simpleName}"
    val block: () -> Unit = {},         // 待执行的任务
) {
    val children: MutableSet<Task> = mutableSetOf() // 子任务列表
}
``` 

核心类

- `TaskList` 负责持有和添加任务
- `TaskManager` 负责调度任务，支持添加开关任务(没有业务仅作为开关，可手动触发完成，并偿试执行其子任务)
  - 无依赖的异步任务，在子线程并行执行
  - 无依赖的同步任务，在主线程顺序执行
  - 有依赖的任务，确保无循环依赖，且被依赖的任务先执行
- `InitManager` 负责找到各模块的任务加载器并开始启动初始化，它使用了一个合规开关来使相关任务在确定合规后执行
 

可以不使用 `InitManager` 收集任务

```kotlin
val taskList = TaskList(app).apply {
    add("task1") { 
    }    
    add("task2", depends = setOf("t1")) { 
    }    
    add("task3", depends = setOf("task1")) { 
    }    
}

val manager = TaskManager(taskList, setOf("t1"))
manager.start()

// ...

// 完成开关任务t1
manager.trigger("t1")
```


## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).
