package com.mapk.fastkfunction

import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.argumentbucket.BucketGenerator
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
        val parameters: List<KParameter> = function.parameters.apply {
            if (isEmpty() || (instance != null && size == 1))
                throw IllegalArgumentException("This function is not require arguments.")
        }

        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true
        // TODO: valueParametersの生成関連の効率化
        valueParameters = parameters.filter { it.kind == KParameter.Kind.VALUE }

        val constructor = function.javaConstructor

        if (constructor != null) {
            bucketGenerator = BucketGenerator(parameters, null)
            fullInitializedFunction = { constructor.newInstance(*it) }
        } else {
            val method = function.javaMethod!!

            // TODO: 必要な場面でインスタンスがnullならthrow
            @Suppress("UNCHECKED_CAST") // methodはTを返せないため強制キャスト
            when (parameters[0].kind) {
                KParameter.Kind.EXTENSION_RECEIVER -> {
                    // TODO: インスタンスに定義した拡張関数 = インスタンスとレシーバ両方が要求される場合throw
                    // 対象が拡張関数なら、instanceはreceiver
                    bucketGenerator = BucketGenerator(parameters, instance)
                    fullInitializedFunction = { method.invoke(null, instance, *it) as T }
                }
                KParameter.Kind.INSTANCE -> {
                    // 対象がインスタンスを要求する関数なら、instanceはobject
                    bucketGenerator = BucketGenerator(parameters, instance)
                    fullInitializedFunction = { method.invoke(instance, *it) as T }
                }
                KParameter.Kind.VALUE -> {
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
                            @Suppress("UNCHECKED_CAST")
                            method.declaringClass.kotlin.objectInstance
                                ?.let { inst -> { method.invoke(inst, *it) as T } }
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
