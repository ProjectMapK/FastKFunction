package com.mapk.fastkfunction

import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.argumentbucket.BucketGenerator
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Constructor as JavaConstructor
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

sealed class FastKFunction<T> {
    abstract val valueParameters: List<KParameter>
    internal abstract val bucketGenerator: BucketGenerator
    fun generateBucket(): ArgumentBucket = bucketGenerator.generateBucket()

    abstract fun callBy(bucket: ArgumentBucket): T
    abstract fun callByCollection(args: Collection<Any?>): T
    abstract fun call(vararg args: Any?): T

    internal class Constructor<T>(
        private val function: KFunction<T>,
        private val constructor: JavaConstructor<T>,
        override val valueParameters: List<KParameter>
    ) : FastKFunction<T>() {
        override val bucketGenerator = BucketGenerator(valueParameters, null)

        override fun callBy(bucket: ArgumentBucket): T = if (bucket.isFullInitialized()) {
            constructor.newInstance(*bucket.getValueArray())
        } else {
            function.callBy(bucket)
        }

        override fun callByCollection(args: Collection<Any?>): T = constructor.newInstance(*args.toTypedArray())

        override fun call(vararg args: Any?): T = constructor.newInstance(*args)
    }

    internal class Function<T>(
        private val function: KFunction<T>,
        override val valueParameters: List<KParameter>
    ) : FastKFunction<T>() {
        override val bucketGenerator = BucketGenerator(valueParameters, null)

        override fun callBy(bucket: ArgumentBucket): T = if (bucket.isFullInitialized()) {
            function.call(*bucket.getValueArray())
        } else {
            function.callBy(bucket)
        }

        override fun callByCollection(args: Collection<Any?>): T = function.call(*args.toTypedArray())

        override fun call(vararg args: Any?): T = function.call(*args)
    }

    internal class TopLevelFunction<T>(
        private val function: KFunction<T>,
        private val method: Method,
        override val valueParameters: List<KParameter>
    ) : FastKFunction<T>() {
        override val bucketGenerator = BucketGenerator(valueParameters, null)

        @Suppress("UNCHECKED_CAST")
        override fun callBy(bucket: ArgumentBucket): T = if (bucket.isFullInitialized()) {
            method.invoke(null, *bucket.getValueArray()) as T
        } else {
            function.callBy(bucket)
        }

        @Suppress("UNCHECKED_CAST")
        override fun callByCollection(args: Collection<Any?>): T = method.invoke(null, *args.toTypedArray()) as T

        @Suppress("UNCHECKED_CAST")
        override fun call(vararg args: Any?): T = method.invoke(null, *args) as T
    }

    internal class TopLevelExtensionFunction<T>(
        private val function: KFunction<T>,
        private val method: Method,
        private val extensionReceiver: Any?,
        override val bucketGenerator: BucketGenerator,
        override val valueParameters: List<KParameter>
    ) : FastKFunction<T>() {
        @Suppress("UNCHECKED_CAST")
        override fun callBy(bucket: ArgumentBucket): T = if (bucket.isFullInitialized()) {
            method.invoke(null, extensionReceiver, *bucket.getValueArray()) as T
        } else {
            function.callBy(bucket)
        }

        @Suppress("UNCHECKED_CAST")
        override fun callByCollection(args: Collection<Any?>): T =
            method.invoke(null, extensionReceiver, *args.toTypedArray()) as T

        @Suppress("UNCHECKED_CAST")
        override fun call(vararg args: Any?): T = method.invoke(null, extensionReceiver, *args) as T
    }

    internal class InstanceFunction<T>(
        private val function: KFunction<T>,
        private val method: Method,
        private val instance: Any,
        override val bucketGenerator: BucketGenerator,
        override val valueParameters: List<KParameter>
    ) : FastKFunction<T>() {
        @Suppress("UNCHECKED_CAST")
        override fun callBy(bucket: ArgumentBucket): T = if (bucket.isFullInitialized()) {
            method.invoke(instance, *bucket.getValueArray()) as T
        } else {
            function.callBy(bucket)
        }

        @Suppress("UNCHECKED_CAST")
        override fun callByCollection(args: Collection<Any?>): T = method.invoke(instance, *args.toTypedArray()) as T

        @Suppress("UNCHECKED_CAST")
        override fun call(vararg args: Any?): T = method.invoke(instance, *args) as T
    }

    companion object {
        private fun List<KParameter>.checkParameters(instance: Any?) = also {
            if (isEmpty() || (instance != null && size == 1))
                throw IllegalArgumentException("This function is not require arguments.")

            if (3 <= size && get(0).kind != KParameter.Kind.VALUE && get(1).kind != KParameter.Kind.VALUE)
                throw IllegalArgumentException("This function is require multiple instances.")
        }

        private fun <T> topLevelFunctionOf(
            function: KFunction<T>, instance: Any?, parameters: List<KParameter>, method: Method
        ): FastKFunction<T> {
            return if (parameters[0].kind == KParameter.Kind.EXTENSION_RECEIVER) {
                // KParameter.Kind.EXTENSION_RECEIVERの要求が有れば確定で拡張関数
                // 対象が拡張関数ならinstanceはreceiver、指定が無ければエラー
                instance ?: throw IllegalArgumentException(
                    "Function requires EXTENSION_RECEIVER instance, but is not present."
                )

                val generator = BucketGenerator(parameters, instance)
                val valueParameters = parameters.subList(1, parameters.size)

                TopLevelExtensionFunction(function, method, instance, generator, valueParameters)
            } else if (method.parameters.size != parameters.size) {
                // javaMethodのパラメータサイズとKFunctionのパラメータサイズが違う場合も拡張関数
                // インスタンスが設定されていれば高速呼び出し、そうじゃなければ通常の関数呼び出し
                instance
                    ?.let {
                        val generator = BucketGenerator(parameters, instance)
                        val valueParameters = parameters.subList(1, parameters.size)

                        TopLevelExtensionFunction(function, method, instance, generator, valueParameters)
                    } ?: Function(function, parameters)
            } else {
                // トップレベル関数
                TopLevelFunction(function, method, parameters)
            }
        }

        private fun <T> instanceFunctionOf(
            function: KFunction<T>, inputtedInstance: Any?, parameters: List<KParameter>, method: Method
        ): FastKFunction<T> {
            val instance = inputtedInstance ?: method.declaringObject

            return if (parameters[0].kind == KParameter.Kind.INSTANCE) {
                instance ?: throw IllegalArgumentException("Function requires INSTANCE parameter, but is not present.")

                val generator = BucketGenerator(parameters, instance)
                val valueParameters = parameters.subList(1, parameters.size)

                InstanceFunction(function, method, instance, generator, valueParameters)
            } else {
                instance
                    ?.let {
                        InstanceFunction(function, method, instance, BucketGenerator(parameters, null), parameters)
                    } ?: Function(function, parameters)
            }
        }

        fun <T> of(function: KFunction<T>, instance: Any? = null): FastKFunction<T> {
            // 引数を要求しないか、複数のインスタンスを求める場合エラーとする
            val parameters: List<KParameter> = function.parameters.checkParameters(instance)

            // この関数には確実にアクセスするためアクセシビリティ書き換え
            function.isAccessible = true

            val constructor = function.javaConstructor

            return if (constructor != null) {
                Constructor(function, constructor, parameters)
            } else {
                val method = function.javaMethod!!

                // Methodがstatic関数ならfunctionはトップレベル関数
                if (Modifier.isStatic(method.modifiers)) {
                    topLevelFunctionOf(function, instance, parameters, method)
                } else {
                    instanceFunctionOf(function, instance, parameters, method)
                }
            }
        }
    }
}
