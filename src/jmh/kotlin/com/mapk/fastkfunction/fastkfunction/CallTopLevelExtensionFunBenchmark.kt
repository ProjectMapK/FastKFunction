package com.mapk.fastkfunction.fastkfunction

import com.mapk.fastkfunction.FastKFunction
import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.benchmarktargets.Constructor5
import com.mapk.fastkfunction.benchmarktargets.topLevelExtensionFun5
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod

@State(Scope.Benchmark)
open class CallTopLevelExtensionFunBenchmark {
    private val receiverInstance = Constructor5(1, 2, 3, 4, 5)

    private val functionByMethodReference: KFunction<Constructor5> = receiverInstance::topLevelExtensionFun5
    private val functionFromClass: KFunction<Constructor5> = Constructor5::topLevelExtensionFun5

    private val argumentMap: Map<KParameter, Any?> = functionByMethodReference.parameters.associateWith { it.index + 1 }

    private val javaMethod: Method = functionByMethodReference.javaMethod!!

    private val fastKFunctionByMethodReferenceWithoutInstance: FastKFunction<Constructor5> =
        FastKFunction.of(functionByMethodReference)
    private val fastKFunctionByMethodReferenceWithInstance: FastKFunction<Constructor5> =
        FastKFunction.of(functionByMethodReference, receiverInstance)

    private val fastKFunctionFromClass: FastKFunction<Constructor5> =
        FastKFunction.of(functionFromClass, receiverInstance)

    private val collection: Collection<Int> = listOf(1, 2, 3, 4, 5)
    private val argumentBucket: ArgumentBucket = fastKFunctionByMethodReferenceWithoutInstance.generateBucket()
        .apply { (0 until 5).forEach { this[it] = it + 1 } }

    @Benchmark
    fun normalCall(): Constructor5 = receiverInstance.topLevelExtensionFun5(1, 2, 3, 4, 5)

    @Benchmark
    fun functionByMethodReferenceCall(): Constructor5 = functionByMethodReference.call(1, 2, 3, 4, 5)

    @Benchmark
    fun functionByMethodReferenceCallBy(): Constructor5 = functionByMethodReference.callBy(argumentMap)

    @Benchmark
    fun functionFromClassCall(): Constructor5 = functionFromClass.call(1, 2, 3, 4, 5)

    @Benchmark
    fun functionFromClassCallBy(): Constructor5 = functionFromClass.callBy(argumentMap)

    @Benchmark
    fun javaMethod(): Constructor5 = javaMethod.invoke(null, receiverInstance, 1, 2, 3, 4, 5) as Constructor5

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
    fun fastKFunctionFromClassCall(): Constructor5 =
        fastKFunctionFromClass.call(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionFromClassCallByCollection(): Constructor5 =
        fastKFunctionFromClass.callByCollection(collection)

    @Benchmark
    fun fastKFunctionFromClassCallBy(): Constructor5 =
        fastKFunctionFromClass.callBy(argumentBucket)
}
