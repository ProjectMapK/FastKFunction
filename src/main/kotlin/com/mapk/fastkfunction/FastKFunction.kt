package com.mapk.fastkfunction

import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.argumentbucket.BucketGenerator
import java.lang.Exception
import java.lang.UnsupportedOperationException
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

class FastKFunction<T>(private val function: KFunction<T>, instance: Any?) {
    val valueParameters: List<KParameter> // 公開するのはバインドに使うパラメータのみ
    private val fullInitializedFunction: (Array<out Any?>) -> T
    private val bucketGenerator: BucketGenerator

    init {
        // 引数を要求しないか、複数のインスタンスを求める場合エラーとする
        val parameters: List<KParameter> = function.parameters.apply {
            if (isEmpty() || (instance != null && size == 1))
                throw IllegalArgumentException("This function is not require arguments.")

            if (3 <= size && get(0).kind != KParameter.Kind.VALUE && get(1).kind != KParameter.Kind.VALUE)
                throw IllegalArgumentException("This function is require multiple instances.")
        }

        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true

        val constructor = function.javaConstructor

        if (constructor != null) {
            valueParameters = parameters
            bucketGenerator = BucketGenerator(parameters, null)
            fullInitializedFunction = { constructor.newInstance(*it) }
        } else {
            val method = function.javaMethod!!

            @Suppress("UNCHECKED_CAST") // methodはTを返せないため強制キャスト
            when (parameters[0].kind) {
                KParameter.Kind.EXTENSION_RECEIVER -> {
                    // 対象が拡張関数ならinstanceはreceiver、指定が無ければエラー
                    instance ?: throw IllegalArgumentException(
                        "Function requires EXTENSION_RECEIVER instance, but is not present."
                    )

                    valueParameters = parameters.subList(1, parameters.size)
                    bucketGenerator = BucketGenerator(parameters, instance)
                    fullInitializedFunction = { method.invoke(null, instance, *it) as T }
                }
                KParameter.Kind.INSTANCE -> {
                    valueParameters = parameters.subList(1, parameters.size)

                    // 対象がインスタンスを要求する関数ならinstanceはobject、与えられたインスタンスがnullでもobjectからの取得を試みる
                    val nonNullInstance = instance ?: try {
                        method.declaringObject!!
                    } catch (e: Exception) {
                        throw IllegalArgumentException("Function requires INSTANCE parameter, but is not present.", e)
                    }

                    bucketGenerator = BucketGenerator(parameters, nonNullInstance)
                    fullInitializedFunction = { method.invoke(nonNullInstance, *it) as T }
                }
                KParameter.Kind.VALUE -> {
                    valueParameters = parameters
                    bucketGenerator = BucketGenerator(parameters, null)

                    fullInitializedFunction = if (instance != null) {
                        // staticメソッドならば渡されたのが拡張関数でinstanceはレシーバと見做す
                        if (Modifier.isStatic(method.modifiers)) {
                            { method.invoke(null, instance, *it) as T }
                        } else {
                            { method.invoke(instance, *it) as T }
                        }
                    } else {
                        try {
                            // 定義先がobjectであればインスタンスを利用した呼び出しを行い、そうでなければ普通に呼び出す
                            method.declaringObject
                                ?.let { instanceFromClass -> { method.invoke(instanceFromClass, *it) as T } }
                                ?: { function.call(*it) }
                        } catch (e: UnsupportedOperationException) {
                            // トップレベル関数でobjectInstanceを取得しようとするとUnsupportedOperationExceptionになるためtryする
                            { function.call(*it) }
                        }
                    }
                }
            }
        }
    }

    fun generateBucket(): ArgumentBucket = bucketGenerator.generateBucket()

    fun callBy(bucket: ArgumentBucket): T = if (bucket.isFullInitialized())
        fullInitializedFunction(bucket.getValueArray())
    else
        function.callBy(bucket)

    fun callByCollection(args: Collection<Any?>): T = fullInitializedFunction(args.toTypedArray())

    @Suppress("UNCHECKED_CAST")
    fun call(vararg args: Any?): T = fullInitializedFunction(args)
}
