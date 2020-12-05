package com.mapk.fastkfunction

import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.benchmarktargets.Constructor5
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.lang.reflect.Constructor
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaConstructor

@State(Scope.Benchmark)
open class CallConstructorBenchmark {
    private val function: KFunction<Constructor5> = ::Constructor5
    private val argumentMap: Map<KParameter, Any?> = function.parameters.associateWith { it.index + 1 }

    private val javaConstructor: Constructor<Constructor5> = function.javaConstructor!!
    private val fastKFunction: FastKFunction<Constructor5> = FastKFunction.of(function, null)
    private val argumentBucket: ArgumentBucket = fastKFunction.generateBucket()
        .apply { (0 until 5).forEach { this[it] = it + 1 } }

    @Benchmark
    fun normalCall(): Constructor5 = Constructor5(1, 2, 3, 4, 5)

    @Benchmark
    fun kFunctionCall(): Constructor5 = function.call(1, 2, 3, 4, 5)

    @Benchmark
    fun kFunctionCallBy(): Constructor5 = function.callBy(argumentMap)

    @Benchmark
    fun javaConstructor(): Constructor5 = javaConstructor.newInstance(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionCall(): Constructor5 = fastKFunction.call(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionCallBy(): Constructor5 = fastKFunction.callBy(argumentBucket)
}
