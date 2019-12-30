package me.ruslanys.ifunny.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableAsync
class ExecutorsConfig
/* : SchedulingConfigurer, AsyncConfigurer {

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = SCHEDULER_POOL_SIZE
        taskScheduler.setThreadNamePrefix("ScheduledExecutor-")
        taskScheduler.initialize()

        taskRegistrar.setTaskScheduler(taskScheduler)
    }

    @Bean(name = ["asyncExecutor"])
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = ASYNC_POOL_SIZE
        executor.setThreadNamePrefix("AsyncExecutor-")
        executor.initialize()
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncUncaughtExceptionHandler { ex, _, _ -> LoggerFactory.getLogger("Async").error("Async error", ex) }
    }

    companion object {
        private const val SCHEDULER_POOL_SIZE = 5
        private const val ASYNC_POOL_SIZE = 10
    }

}*/
