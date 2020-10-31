package com.mapk.fastkfunction

import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.argumentbucket.BucketGenerator
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

class FastKFunction<T>(private val function: KFunction<T>, instance: Any?) {
    val valueParameters: List<KParameter> // 公開するのはバインドに使うパラメータのみ
    private val fullInitializedFunction: (Array<Any?>) -> T
    private val bucketGenerator: BucketGenerator

    init {
        val parameters: List<KParameter> = function.parameters.apply {
            if (isEmpty() || (instance != null && size == 1))
                throw IllegalArgumentException("This function is not require arguments.")
        }

        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true

        bucketGenerator = BucketGenerator(parameters)
        valueParameters = parameters.filter { it.kind == KParameter.Kind.VALUE }

        val constructor = function.javaConstructor

        fullInitializedFunction = when {
            constructor != null -> {
                { constructor.newInstance(*it) }
            }
            instance != null -> {
                val method = function.javaMethod!!

                @Suppress("UNCHECKED_CAST")
                { method.invoke(instance, *it) as T }
            }
            else -> {
                val method = function.javaMethod!!

                // 定義先がobjectであればインスタンスを利用した呼び出しを行う
                @Suppress("UNCHECKED_CAST")
                method.declaringClass.kotlin.objectInstance?.let { inst ->
                    { method.invoke(inst, *it) as T }
                } ?: { function.call(*it) }
            }
        }
    }

    fun generateBucket(): ArgumentBucket = bucketGenerator.generateBucket()

    fun call(bucket: ArgumentBucket): T = if (bucket.isFullInitialized())
        fullInitializedFunction(bucket.getValueArray())
    else
        function.callBy(bucket)

    fun call(args: Collection<Any?>): T = fullInitializedFunction(args.toTypedArray())

    fun call(args: Array<Any?>): T = fullInitializedFunction(args)
}
