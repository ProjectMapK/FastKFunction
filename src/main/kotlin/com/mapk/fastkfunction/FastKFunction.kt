package com.mapk.fastkfunction

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

class FastKFunction<T>(private val function: KFunction<T>, instance: Any?) {
    val valueParameters: List<KParameter> = function.valueParameters
    private val fullInitializedFunction: (Array<Any?>) -> T

    // TODO: バケツジェネレータの追加

    init {
        val constructor = function.javaConstructor

        fullInitializedFunction = when {
            constructor != null -> {
                { constructor.newInstance(*it) }
            }
            instance != null -> {
                val method = function.javaMethod!!
                val size = function.parameters.size

                @Suppress("UNCHECKED_CAST") { method.invoke(instance, *(it.copyOfRange(1, size))) as T }
            }
            else -> {
                { function.call(*it) }
            }
        }
    }

    fun call(bucket: ArgumentBucket): T = if (bucket.isFullInitialized())
        fullInitializedFunction(bucket.valueArray)
    else
        function.callBy(bucket)

    fun call(args: Collection<Any?>): T = fullInitializedFunction(args.toTypedArray())

    fun call(args: Array<Any?>): T = fullInitializedFunction(args)
}
