package com.mapk.fastkfunction

import java.lang.reflect.Method

/**
 * Get object instance if receiver declared in object.
 *
 * @receiver JavaMethod.
 * @return Method.declaringClass.kotlin.objectInstance
 * @throws UnsupportedOperationException Method declared on top level.
 */
internal val Method.declaringObject: Any? get() = declaringClass.kotlin.objectInstance
