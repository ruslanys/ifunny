package me.ruslanys.ifunny.grab

interface SuspendedEventListener<in T> {

    suspend fun handleEvent(event: T)

}
