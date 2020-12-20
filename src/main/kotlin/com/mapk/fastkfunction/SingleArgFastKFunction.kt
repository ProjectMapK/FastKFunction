package com.mapk.fastkfunction

import org.jetbrains.annotations.TestOnly
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod
import java.lang.reflect.Constructor as JavaConstructor

sealed class SingleArgFastKFunction<T> {
    abstract val valueParameter: KParameter
    abstract fun call(arg: Any?): T

    internal class Constructor<T>(
        override val valueParameter: KParameter,
        private val constructor: JavaConstructor<T>
    ) : SingleArgFastKFunction<T>() {
        override fun call(arg: Any?): T = constructor.newInstance(arg)
    }

    internal class Function<T>(
        override val valueParameter: KParameter,
        private val function: KFunction<T>
    ) : SingleArgFastKFunction<T>() {
        override fun call(arg: Any?): T = function.call(arg)
    }

    internal class TopLevelFunction<T>(
        override val valueParameter: KParameter,
        private val method: Method
    ) : SingleArgFastKFunction<T>() {
        @Suppress("UNCHECKED_CAST")
        override fun call(arg: Any?): T = method.invoke(null, arg) as T
    }

    internal class TopLevelExtensionFunction<T>(
        override val valueParameter: KParameter,
        private val method: Method,
        private val extensionReceiver: Any
    ) : SingleArgFastKFunction<T>() {
        @Suppress("UNCHECKED_CAST")
        override fun call(arg: Any?): T = method.invoke(null, extensionReceiver, arg) as T
    }

    internal class InstanceFunction<T>(
        override val valueParameter: KParameter,
        private val method: Method,
        private val instance: Any
    ) : SingleArgFastKFunction<T>() {
        @Suppress("UNCHECKED_CAST")
        override fun call(arg: Any?): T = method.invoke(instance, arg) as T
    }

    companion object {
        @TestOnly
        internal fun List<KParameter>.checkParameters() = also {
            val requireInstanceParameter = !isEmpty() && this[0].kind != KParameter.Kind.VALUE

            if (isEmpty() || (requireInstanceParameter && size == 1))
                throw IllegalArgumentException("This function is not require arguments.")

            if (!(this.size == 1 || (this.size == 2 && requireInstanceParameter)))
                throw IllegalArgumentException("This function is require multiple arguments.")

            if (this.size == 2 && this[1].kind != KParameter.Kind.VALUE)
                throw IllegalArgumentException("This function is require multiple instances.")
        }

        @TestOnly
        internal fun <T> topLevelFunctionOf(
            function: KFunction<T>,
            instance: Any?,
            parameters: List<KParameter>,
            method: Method
        ): SingleArgFastKFunction<T> = when {
            // KParameter.Kind.EXTENSION_RECEIVERの要求が有れば確定で拡張関数
            parameters[0].kind == KParameter.Kind.EXTENSION_RECEIVER -> {
                // 対象が拡張関数ならinstanceはreceiver、指定が無ければエラー
                instance ?: throw IllegalArgumentException(
                    "Function requires EXTENSION_RECEIVER instance, but is not present."
                )

                TopLevelExtensionFunction(parameters[1], method, instance)
            }
            // javaMethodのパラメータサイズとKFunctionのパラメータサイズが違う場合も拡張関数
            // インスタンスが設定されていれば高速呼び出し、そうじゃなければ通常の関数呼び出し
            method.parameters.size != parameters.size ->
                instance
                    ?.let { TopLevelExtensionFunction(parameters[0], method, instance) }
                    ?: Function(parameters[0], function)
            // トップレベル関数
            else -> TopLevelFunction(parameters[0], method)
        }

        @TestOnly
        internal fun <T> instanceFunctionOf(
            function: KFunction<T>,
            inputtedInstance: Any?,
            parameters: List<KParameter>,
            method: Method
        ): SingleArgFastKFunction<T> {
            val instance = inputtedInstance ?: method.declaringObject

            return when {
                parameters[0].kind == KParameter.Kind.INSTANCE ->
                    instance
                        ?.let {
                            val instanceClazz = it::class

                            method.declaringClass.kotlin.also { requiredClazz ->
                                if (!requiredClazz.isSuperclassOf(instanceClazz))
                                    throw IllegalArgumentException(
                                        "INSTANCE parameter required ${instanceClazz.simpleName}, " +
                                                "but ${instanceClazz.simpleName} is present."
                                    )
                            }

                            InstanceFunction(parameters[1], method, it)
                        }
                        ?: throw IllegalArgumentException("Function requires INSTANCE parameter, but is not present.")
                instance != null -> {
                    val instanceClazz = instance::class

                    method.declaringClass.kotlin.also {
                        if (!it.isSuperclassOf(instanceClazz))
                            throw IllegalArgumentException(
                                "INSTANCE parameter required ${it.simpleName}, " +
                                        "but ${instanceClazz.simpleName} is present."
                            )
                    }

                    InstanceFunction(parameters[0], method, instance)
                }
                else -> Function(parameters[0], function)
            }
        }

        fun <T> of(function: KFunction<T>, instance: Any? = null): SingleArgFastKFunction<T> {
            // 引数を要求しないか、複数のインスタンスを求める場合エラーとする
            val parameters: List<KParameter> = function.parameters.checkParameters()

            // この関数には確実にアクセスするためアクセシビリティ書き換え
            function.isAccessible = true

            val constructor = function.javaConstructor

            return if (constructor != null) {
                Constructor(parameters[0], constructor)
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
