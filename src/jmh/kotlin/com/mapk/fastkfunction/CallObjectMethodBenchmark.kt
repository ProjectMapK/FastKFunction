package com.mapk.fastkfunction

import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.benchmarktargets.Constructor5
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
    private val objectInstance = Constructor5::class.companionObjectInstance!!

    private val functionByMethodReference: KFunction<Constructor5> = (Constructor5)::companionObjectFun5
    @Suppress("UNCHECKED_CAST")
    private val functionByReflection: KFunction<Constructor5> = objectInstance::class
        .functions
        .first { it.name == "companionObjectFun5" } as KFunction<Constructor5>

    private val argumentMap: Map<KParameter, Any?> = functionByMethodReference.parameters.associateWith { it.index + 1 }

    private val javaMethod: Method = functionByMethodReference.javaMethod!!

    private val fastKFunctionByMethodReferenceWithoutInstance: FastKFunction<Constructor5> =
        FastKFunction.of(functionByMethodReference)
    private val fastKFunctionByMethodReferenceWithInstance: FastKFunction<Constructor5> =
        FastKFunction.of(functionByMethodReference, objectInstance)

    private val fastKFunctionByReflectionWithoutInstance: FastKFunction<Constructor5> =
        FastKFunction.of(functionByReflection)
    private val fastKFunctionByReflectionWithInstance: FastKFunction<Constructor5> =
        FastKFunction.of(functionByReflection, objectInstance)

    private val collection: Collection<Int> = listOf(1, 2, 3, 4, 5)
    private val argumentBucket: ArgumentBucket = fastKFunctionByMethodReferenceWithoutInstance.generateBucket()
        .apply { (0 until 5).forEach { this[it] = it + 1 } }

    @Benchmark
    fun normalCall(): Constructor5 = Constructor5.companionObjectFun5(1, 2, 3, 4, 5)

    @Benchmark
    fun functionByMethodReferenceCall(): Constructor5 = functionByMethodReference.call(1, 2, 3, 4, 5)

    @Benchmark
    fun functionByMethodReferenceCallBy(): Constructor5 = functionByMethodReference.callBy(argumentMap)

    @Benchmark
    fun functionByReflectionCall(): Constructor5 = functionByReflection.call(1, 2, 3, 4, 5)

    @Benchmark
    fun functionByReflectionCallBy(): Constructor5 = functionByReflection.callBy(argumentMap)

    @Benchmark
    fun javaMethod(): Constructor5 = javaMethod.invoke(objectInstance, 1, 2, 3, 4, 5) as Constructor5

    @Benchmark
    fun fastKFunctionByMethodReferenceWithoutInstanceCall(): Constructor5 =
        fastKFunctionByMethodReferenceWithoutInstance.call(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionByMethodReferenceWithoutInstanceCallByCollection(): Constructor5 =
        fastKFunctionByMethodReferenceWithoutInstance.callByCollection(collection)

    @Benchmark
    fun fastKFunctionByMethodReferenceWithoutInstanceCallBy(): Constructor5 =
        fastKFunctionByMethodReferenceWithoutInstance.callBy(argumentBucket)

    @Benchmark
    fun fastKFunctionByMethodReferenceWithInstanceCall(): Constructor5 =
        fastKFunctionByMethodReferenceWithInstance.call(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionByMethodReferenceWithInstanceCallByCollection(): Constructor5 =
        fastKFunctionByMethodReferenceWithInstance.callByCollection(collection)

    @Benchmark
    fun fastKFunctionByMethodReferenceWithInstanceCallBy(): Constructor5 =
        fastKFunctionByMethodReferenceWithInstance.callBy(argumentBucket)

    @Benchmark
    fun fastKFunctionByReflectionWithoutInstanceCall(): Constructor5 =
        fastKFunctionByReflectionWithoutInstance.call(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionByReflectionWithoutInstanceCallByCollection(): Constructor5 =
        fastKFunctionByReflectionWithoutInstance.callByCollection(collection)

    @Benchmark
    fun fastKFunctionByReflectionWithoutInstanceCallBy(): Constructor5 =
        fastKFunctionByReflectionWithoutInstance.callBy(argumentBucket)

    @Benchmark
    fun fastKFunctionByReflectionWithInstanceCall(): Constructor5 =
        fastKFunctionByReflectionWithInstance.call(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionByReflectionWithInstanceCallByCollection(): Constructor5 =
        fastKFunctionByReflectionWithInstance.callByCollection(collection)

    @Benchmark
    fun fastKFunctionByReflectionWithInstanceCallBy(): Constructor5 =
        fastKFunctionByReflectionWithInstance.callBy(argumentBucket)
}
