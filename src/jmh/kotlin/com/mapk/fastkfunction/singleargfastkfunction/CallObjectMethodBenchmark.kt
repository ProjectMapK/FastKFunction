package com.mapk.fastkfunction.singleargfastkfunction

import com.mapk.fastkfunction.FastKFunction
import com.mapk.fastkfunction.SingleArgFastKFunction
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

@State(Scope.Benchmark)
open class CallObjectMethodBenchmark {
    private val argument = 1

    private val objectInstance = Constructor1::class.companionObjectInstance!!

    private val functionByMethodReference: KFunction<Constructor1> = (Constructor1)::companionObjectFun1
    @Suppress("UNCHECKED_CAST")
    private val functionByReflection: KFunction<Constructor1> = objectInstance::class
        .functions
        .first { it.name == "companionObjectFun5" } as KFunction<Constructor1>

    private val argumentMap: Map<KParameter, Any?> = mapOf(functionByMethodReference.parameters.single() to argument)

    private val javaMethod: Method = functionByMethodReference.javaMethod!!

    private val fastKFunctionByMethodReferenceWithoutInstance: FastKFunction<Constructor1> =
        FastKFunction.of(functionByMethodReference)
    private val fastKFunctionByMethodReferenceWithInstance: FastKFunction<Constructor1> =
        FastKFunction.of(functionByMethodReference, objectInstance)

    private val fastKFunctionByReflectionWithoutInstance: FastKFunction<Constructor1> =
        FastKFunction.of(functionByReflection)
    private val fastKFunctionByReflectionWithInstance: FastKFunction<Constructor1> =
        FastKFunction.of(functionByReflection, objectInstance)

    private val singleArgFastKFunctionByMethodReferenceWithoutInstance: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(functionByMethodReference)
    private val singleArgFastKFunctionByMethodReferenceWithInstance: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(functionByMethodReference, objectInstance)

    private val singleArgFastKFunctionByReflectionWithoutInstance: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(functionByReflection)
    private val singleArgFastKFunctionByReflectionWithInstance: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(functionByReflection, objectInstance)

    @Benchmark
    fun normalCall(): Constructor1 = Constructor1.companionObjectFun1(argument)

    @Benchmark
    fun functionByMethodReferenceCall(): Constructor1 = functionByMethodReference.call(argument)

    @Benchmark
    fun functionByMethodReferenceCallBy(): Constructor1 = functionByMethodReference.callBy(argumentMap)

    @Benchmark
    fun functionByReflectionCall(): Constructor1 = functionByReflection.call(argument)

    @Benchmark
    fun functionByReflectionCallBy(): Constructor1 = functionByReflection.callBy(argumentMap)

    @Benchmark
    fun javaMethod(): Constructor1 = javaMethod.invoke(objectInstance, argument) as Constructor1

    @Benchmark
    fun fastKFunctionByMethodReferenceWithoutInstanceCall(): Constructor1 =
        fastKFunctionByMethodReferenceWithoutInstance.call(argument)

    @Benchmark
    fun fastKFunctionByMethodReferenceWithInstanceCall(): Constructor1 =
        fastKFunctionByMethodReferenceWithInstance.call(argument)

    @Benchmark
    fun fastKFunctionByReflectionWithoutInstanceCall(): Constructor1 =
        fastKFunctionByReflectionWithoutInstance.call(argument)

    @Benchmark
    fun fastKFunctionByReflectionWithInstanceCall(): Constructor1 =
        fastKFunctionByReflectionWithInstance.call(argument)

    @Benchmark
    fun singleArgFastKFunctionByMethodReferenceWithoutInstanceCall(): Constructor1 =
        singleArgFastKFunctionByMethodReferenceWithoutInstance.call(argument)

    @Benchmark
    fun singleArgFastKFunctionByMethodReferenceWithInstanceCall(): Constructor1 =
        singleArgFastKFunctionByMethodReferenceWithInstance.call(argument)

    @Benchmark
    fun singleArgFastKFunctionByReflectionWithoutInstanceCall(): Constructor1 =
        singleArgFastKFunctionByReflectionWithoutInstance.call(argument)

    @Benchmark
    fun singleArgFastKFunctionByReflectionWithInstanceCall(): Constructor1 =
        singleArgFastKFunctionByReflectionWithInstance.call(argument)
}
