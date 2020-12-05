package com.mapk.fastkfunction

import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.benchmarktargets.Constructor5
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod

@State(Scope.Benchmark)
open class CallInstanceMethodBenchmark {
    private val instance = Constructor5(1, 2, 3, 4, 5)
    private val function: KFunction<Constructor5> = instance::instanceFun5
    private val argumentMap: Map<KParameter, Any?> = function.parameters.associateWith { it.index + 1 }

    private val javaMethod: Method = function.javaMethod!!
    private val fastKFunctionWithoutInstance: FastKFunction<Constructor5> = FastKFunction(function, null)
    private val fastKFunctionWithInstance: FastKFunction<Constructor5> = FastKFunction(function, instance)
    private val argumentBucket: ArgumentBucket = fastKFunctionWithoutInstance.generateBucket()
        .apply { (0 until 5).forEach { this[it] = it + 1 } }

    @Benchmark
    fun normalCall(): Constructor5 = instance.instanceFun5(1, 2, 3, 4, 5)

    @Benchmark
    fun kFunctionCall(): Constructor5 = function.call(1, 2, 3, 4, 5)

    @Benchmark
    fun kFunctionCallBy(): Constructor5 = function.callBy(argumentMap)

    @Benchmark
    fun javaMethod(): Constructor5 = javaMethod.invoke(instance, 1, 2, 3, 4, 5) as Constructor5

    @Benchmark
    fun fastKFunctionWithoutInstanceCall(): Constructor5 = fastKFunctionWithoutInstance.call(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionWithoutInstanceCallBy(): Constructor5 = fastKFunctionWithoutInstance.callBy(argumentBucket)

    @Benchmark
    fun fastKFunctionWithInstanceCall(): Constructor5 = fastKFunctionWithInstance.call(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionWithInstanceCallBy(): Constructor5 = fastKFunctionWithInstance.callBy(argumentBucket)
}
