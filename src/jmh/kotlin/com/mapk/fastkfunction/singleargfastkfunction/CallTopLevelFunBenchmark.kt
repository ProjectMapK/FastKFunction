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
open class CallTopLevelFunBenchmark {
    private val argument = 1

    private val function: KFunction<Constructor1> = ::topLevelFun1
    private val argumentMap: Map<KParameter, Any?> = mapOf(function.parameters.single() to argument)

    private val javaMethod: Method = function.javaMethod!!
    private val fastKFunction: FastKFunction<Constructor1> = FastKFunction.of(function, null)
    private val singleArgFastKFunction: SingleArgFastKFunction<Constructor1> =
        SingleArgFastKFunction.of(function, null)

    @Benchmark
    fun normalCall(): Constructor1 = topLevelFun1(argument)

    @Benchmark
    fun kFunctionCall(): Constructor1 = function.call(argument)

    @Benchmark
    fun kFunctionCallBy(): Constructor1 = function.callBy(argumentMap)

    @Benchmark
    fun javaMethod(): Constructor1 = javaMethod.invoke(null, argument) as Constructor1

    @Benchmark
    fun fastKFunctionCall(): Constructor1 = fastKFunction.call(argument)

    @Benchmark
    fun singleArgFastKFunctionCall(): Constructor1 = singleArgFastKFunction.call(argument)
}
