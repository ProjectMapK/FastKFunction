package com.mapk.fastkfunction

import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

class SingleArgFastKFunction<T>(function: KFunction<T>, instance: Any?) {
    val call: (Any?) -> T

    init {
        val constructor = function.javaConstructor

        call = when {
            constructor != null -> {
                { constructor.newInstance(it) }
            }
            instance != null -> {
                val method = function.javaMethod!!

                @Suppress("UNCHECKED_CAST") { method.invoke(instance, it) as T }
            }
            else -> {
                { function.call(it) }
            }
        }
    }
}
