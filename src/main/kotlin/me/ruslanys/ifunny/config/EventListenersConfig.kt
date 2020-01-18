package me.ruslanys.ifunny.config

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import me.ruslanys.ifunny.Application
import me.ruslanys.ifunny.grab.SuspendedEventListener
import me.ruslanys.ifunny.grab.event.GrabEvent
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct
import kotlin.math.max
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

@Configuration
class EventListenersConfig(
        listeners: List<SuspendedEventListener<*>>,
        private val eventChannel: Channel<GrabEvent>
) {

    private val listeners = listeners.groupBy { listener ->
        listener::class.supertypes.first { it.isSubtypeOf(SuspendedEventListener::class.starProjectedType) }.arguments[0].type
    }

    @PostConstruct
    fun initCoroutines() = GlobalScope.launch {
        val threadsNumber = max(Runtime.getRuntime().availableProcessors(), 8)
        val coroutinesNumber = threadsNumber * 2

        val workerContext = newFixedThreadPoolContext(threadsNumber, "worker")

        repeat(coroutinesNumber) {
            launch(workerContext) {
                handleEvents()
            }
        }
    }

    private suspend fun handleEvents() {
        for (event in eventChannel) {
            listeners[event::class.starProjectedType]?.forEach {
                try {
                    (it as SuspendedEventListener<Any>).handleEvent(event)
                } catch (ex: Exception) {
                    log.error("Unexpected exception occurred invoking listener: ${it::class}, with event: $event", ex)
                }
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Application::class.java)
    }

}
