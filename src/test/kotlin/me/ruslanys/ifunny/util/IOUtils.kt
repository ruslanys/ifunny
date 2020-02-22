package me.ruslanys.ifunny.util

inline fun <reified T> readResource(path: String): String =
        T::class.java.getResourceAsStream(path).bufferedReader().use {
            it.readText()
        }
