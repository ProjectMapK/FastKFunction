package com.mapk.fastkfunction.singleargfastkfunction

import com.mapk.fastkfunction.FastKFunction
import com.mapk.fastkfunction.SingleArgFastKFunction
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod

@State(Scope.Benchmark)
open class CallTopLevelExtensionFunBenchmark {
    private val argument = 1

    private val receiverInstance = Constructor1(argument)

    private val functionByMethodReference: KFunction<Constructor1> = receiverInstance::topLevelExtensionFun1
    private val functionFromClass: KFunction<Constructor1> = Constructor1::topLevelExtensionFun1

    private val argumentMap: Map<KParameter, Any?> = mapOf(functionByMethodReference.parameters.single() to argument)
    private val argumentMapWithInstance: Map<KParameter, Any?> = functionFromClass.parameters.let {
        mapOf(
            it[0] to receiverInstance,
            it[1] to argument
        )
    }

    private val javaMethod: Method = functionByMethodReference.javaMethod!!

    private val fastKFunctionByMethodReferenceWithoutInstance: FastKFunction<Constructor1> =
        FastKFunction.of(functionByMethodReference)
    private val fastKFunctionByMethodReferenceWithInstance: FastKFunction<Constructor1> =
        FastKFunction.of(functionByMethodReference, receiverInstance)

    private val fastKFunctionFromClass: FastKFunction<Constructor1> =
        FastKFunction.of(functionFromClass, receiverInstance)

    private val singleArgFastKFunctionByMethodReferenceWithoutInstance: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(functionByMethodReference)
    private val singleArgFastKFunctionByMethodReferenceWithInstance: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(functionByMethodReference, receiverInstance)

    private val singleArgFastKFunctionFromClass: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(functionFromClass, receiverInstance)

    @Benchmark
    fun normalCall(): Constructor1 = receiverInstance.topLevelExtensionFun1(argument)

    @Benchmark
    fun functionByMethodReferenceCall(): Constructor1 = functionByMethodReference.call(argument)

    @Benchmark
    fun functionByMethodReferenceCallBy(): Constructor1 = functionByMethodReference.callBy(argumentMap)

    @Benchmark
    fun functionFromClassCall(): Constructor1 = functionFromClass.call(receiverInstance, argument)

    @Benchmark
    fun functionFromClassCallBy(): Constructor1 = functionFromClass.callBy(argumentMapWithInstance)

    @Benchmark
    fun javaMethod(): Constructor1 = javaMethod.invoke(null, receiverInstance, argument) as Constructor1

    @Benchmark
    fun fastKFunctionByMethodReferenceWithoutInstanceCall(): Constructor1 =
        fastKFunctionByMethodReferenceWithoutInstance.call(argument)

    @Benchmark
    fun fastKFunctionByMethodReferenceWithInstanceCall(): Constructor1 =
        fastKFunctionByMethodReferenceWithInstance.call(argument)

    @Benchmark
    fun fastKFunctionFromClassCall(): Constructor1 = fastKFunctionFromClass.call(argument)

    @Benchmark
    fun singleArgFastKFunctionByMethodReferenceWithoutInstanceCall(): Constructor1 =
        singleArgFastKFunctionByMethodReferenceWithoutInstance.call(argument)

    @Benchmark
    fun singleArgFastKFunctionByMethodReferenceWithInstanceCall(): Constructor1 =
        singleArgFastKFunctionByMethodReferenceWithInstance.call(argument)

    @Benchmark
    fun singleArgFastKFunctionFromClassCall(): Constructor1 = singleArgFastKFunctionFromClass.call(argument)
}
