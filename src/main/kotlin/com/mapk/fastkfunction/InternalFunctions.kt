package com.mapk.fastkfunction

import java.lang.reflect.Method
import kotlin.reflect.KParameter

/**
 * Get object instance if receiver declared in object.
 *
 * @receiver JavaMethod.
 * @return Method.declaringClass.kotlin.objectInstance
 * @throws UnsupportedOperationException Method declared on top level.
 */
internal val Method.declaringObject: Any? get() = declaringClass.kotlin.objectInstance

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
