package com.mapk.fastkfunction.singleargfastkfunction

import com.mapk.fastkfunction.FastKFunction
import com.mapk.fastkfunction.SingleArgFastKFunction
import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod

@State(Scope.Benchmark)
open class CallInstanceMethodBenchmark {
    private val argument = 1

    private val instance = Constructor1(1)
    private val function: KFunction<Constructor1> = instance::instanceFun1
    private val argumentMap: Map<KParameter, Any?> = mapOf(function.parameters.single() to argument)

    private val javaMethod: Method = function.javaMethod!!
    private val fastKFunctionWithoutInstance: FastKFunction<Constructor1> = FastKFunction.of(function, null)
    private val fastKFunctionWithInstance: FastKFunction<Constructor1> = FastKFunction.of(function, instance)
    private val singleArgFastKFunctionWithoutInstance: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(function, null)
    private val singleArgFastKFunctionWithInstance: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(function, instance)

    @Benchmark
    fun normalCall(): Constructor1 = instance.instanceFun1(argument)

    @Benchmark
    fun kFunctionCall(): Constructor1 = function.call(argument)

    @Benchmark
    fun kFunctionCallBy(): Constructor1 = function.callBy(argumentMap)

    @Benchmark
    fun javaMethod(): Constructor1 = javaMethod.invoke(instance, argument) as Constructor1

    @Benchmark
    fun fastKFunctionWithoutInstanceCall(): Constructor1 = fastKFunctionWithoutInstance.call(argument)

    @Benchmark
    fun fastKFunctionWithInstanceCall(): Constructor1 = fastKFunctionWithInstance.call(argument)

    @Benchmark
    fun singleArgFastKFunctionWithoutInstanceCall(): Constructor1 = singleArgFastKFunctionWithoutInstance.call(argument)

    @Benchmark
    fun singleArgFastKFunctionWithInstanceCall(): Constructor1 = singleArgFastKFunctionWithInstance.call(argument)
}
