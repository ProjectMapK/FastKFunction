package com.mapk.fastkfunction.singleargfastkfunction

import com.mapk.fastkfunction.FastKFunction
import com.mapk.fastkfunction.SingleArgFastKFunction
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.lang.reflect.Constructor
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaConstructor

@State(Scope.Benchmark)
open class CallConstructorBenchmark {
    private val argument = 1

    private val function: KFunction<Constructor1> = ::Constructor1
    private val argumentMap: Map<KParameter, Any?> = mapOf(function.parameters.single() to argument)

    private val javaConstructor: Constructor<Constructor1> = function.javaConstructor!!
    private val fastKFunction: FastKFunction<Constructor1> = FastKFunction.of(function)
    private val singleArgFastKFunction: SingleArgFastKFunction<Constructor1> = SingleArgFastKFunction.of(function)

    @Benchmark
    fun normalCall(): Constructor1 = Constructor1(argument)

    @Benchmark
    fun kFunctionCall(): Constructor1 = function.call(argument)

    @Benchmark
    fun kFunctionCallBy(): Constructor1 = function.callBy(argumentMap)

    @Benchmark
    fun javaConstructor(): Constructor1 = javaConstructor.newInstance(argument)

    @Benchmark
    fun fastKFunctionCall(): Constructor1 = fastKFunction.call(argument)

    @Benchmark
    fun singleArgFastKFunctionCall(): Constructor1 = singleArgFastKFunction.call(argument)
}
