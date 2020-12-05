package com.mapk.fastkfunction

import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.argumentbucket.BucketGenerator
import java.lang.Exception
import java.lang.UnsupportedOperationException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

class FastKFunction<T>(private val function: KFunction<T>, instance: Any? = null) {
    val valueParameters: List<KParameter> // 公開するのはバインドに使うパラメータのみ
    private val fullInitializedFunction: (Array<out Any?>) -> T
    private val bucketGenerator: BucketGenerator

    companion object {
        private fun List<KParameter>.checkParameters(instance: Any?) = apply {
            if (isEmpty() || (instance != null && size == 1))
                throw IllegalArgumentException("This function is not require arguments.")

            if (3 <= size && get(0).kind != KParameter.Kind.VALUE && get(1).kind != KParameter.Kind.VALUE)
                throw IllegalArgumentException("This function is require multiple instances.")
        }

        private fun <T> getFunctionCall(function: KFunction<T>): (Array<out Any?>) -> T = { function.call(*it) }

        // methodはTを返せないため強制キャスト
        @Suppress("UNCHECKED_CAST")
        private fun <T> getStaticMethodCall(method: Method, instance: Any): (Array<out Any?>) -> T =
            { method.invoke(null, instance, *it) as T }

        @Suppress("UNCHECKED_CAST")
        private fun <T> getInstanceMethodCall(method: Method, instance: Any): (Array<out Any?>) -> T =
            { method.invoke(instance, *it) as T }
    }

    init {
        // 引数を要求しないか、複数のインスタンスを求める場合エラーとする
        val parameters: List<KParameter> = function.parameters.checkParameters(instance)

        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true

        val constructor = function.javaConstructor

        if (constructor != null) {
            valueParameters = parameters
            bucketGenerator = BucketGenerator(parameters, null)
            fullInitializedFunction = { constructor.newInstance(*it) }
        } else {
            val method = function.javaMethod!!

            when (parameters[0].kind) {
                KParameter.Kind.EXTENSION_RECEIVER -> {
                    // 対象が拡張関数ならinstanceはreceiver、指定が無ければエラー
                    instance ?: throw IllegalArgumentException(
                        "Function requires EXTENSION_RECEIVER instance, but is not present."
                    )

                    valueParameters = parameters.subList(1, parameters.size)
                    bucketGenerator = BucketGenerator(parameters, instance)
                    fullInitializedFunction = getStaticMethodCall(method, instance)
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
                    fullInitializedFunction = getInstanceMethodCall(method, nonNullInstance)
                }
                KParameter.Kind.VALUE -> {
                    valueParameters = parameters
                    bucketGenerator = BucketGenerator(parameters, null)

                    fullInitializedFunction = if (instance != null) {
                        // staticメソッドならば渡されたのが拡張関数でinstanceはレシーバと見做す
                        if (Modifier.isStatic(method.modifiers)) {
                            getStaticMethodCall(method, instance)
                        } else {
                            getInstanceMethodCall(method, instance)
                        }
                    } else {
                        // staticメソッドかつ引数の数がKFunctionの引数の数と変わらない場合はトップレベル関数（= 実体はstatic関数）
                        if (Modifier.isStatic(method.modifiers) && parameters.size == method.parameters.size) {
                            @Suppress("UNCHECKED_CAST")
                            { method.invoke(null, *it) as T }
                        } else {
                            try {
                                // 定義先がobjectであればインスタンスを利用した呼び出しを行い、そうでなければ普通に呼び出す
                                method.declaringObject
                                    ?.let { instanceFromClass -> getInstanceMethodCall(method, instanceFromClass) }
                                    ?: getFunctionCall(function)
                            } catch (e: UnsupportedOperationException) {
                                // トップレベル関数でobjectInstanceを取得しようとするとUnsupportedOperationExceptionになるためtryする
                                getFunctionCall(function)
                            }
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
