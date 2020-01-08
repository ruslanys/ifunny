package me.ruslanys.ifunny.controller.validation

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [(SortValidator::class)])
annotation class SortConstraint(
        val message: String = "Collection is not contains value",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)
