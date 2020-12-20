package com.mapk.fastkfunction

import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSuperclassOf

/**
 * Get object instance if receiver declared in object.
 *
 * @receiver JavaMethod.
 * @return Method.declaringClass.kotlin.objectInstance
 * @throws UnsupportedOperationException Method declared on top level.
 */
internal val Method.declaringObject: Any? get() = declaringClass.kotlin.objectInstance

/**
 * Get KParameters KClass.
 *
 * @receiver KParameter.
 * @returns KClass.
 */
internal val KParameter.clazz: KClass<*> get() = this.type.classifier as KClass<*>

/**
 * Check instance class is valid.
 *
 * @param expected Required clazz.
 * @param actual Actual clazz.
 * @throws IllegalArgumentException If actual is not required class.
 */
internal fun checkInstanceClass(expected: KClass<*>, actual: KClass<*>) {
    if (!expected.isSuperclassOf(actual))
        throw IllegalArgumentException(
            "INSTANCE parameter required ${expected.simpleName}, but ${actual.simpleName} is present."
        )
}

/**
 * Throw IllegalArgumentException if instance is null.
 *
 * @receiver Instance parameter.
 * @param kind Instance Kind.
 * @return instance.
 * @throws IllegalArgumentException Instance is null.
 */
internal fun <T : Any> T?.instanceOrThrow(kind: KParameter.Kind): T =
    this ?: throw IllegalArgumentException("Function requires ${kind.name} parameter, but is not present.")
