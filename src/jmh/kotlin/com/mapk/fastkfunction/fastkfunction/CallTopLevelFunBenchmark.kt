package com.mapk.fastkfunction.fastkfunction

import com.mapk.fastkfunction.FastKFunction
import com.mapk.fastkfunction.argumentbucket.ArgumentBucket
import com.mapk.fastkfunction.benchmarktargets.Constructor5
import com.mapk.fastkfunction.benchmarktargets.topLevelFun5
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod

@State(Scope.Benchmark)
open class CallTopLevelFunBenchmark {
    private val function: KFunction<Constructor5> = ::topLevelFun5
    private val argumentMap: Map<KParameter, Any?> = function.parameters.associateWith { it.index + 1 }

    private val javaMethod: Method = function.javaMethod!!
    private val fastKFunction: FastKFunction<Constructor5> = FastKFunction.of(function, null)
    private val collection: Collection<Int> = listOf(1, 2, 3, 4, 5)
    private val argumentBucket: ArgumentBucket = fastKFunction.generateBucket()
        .apply { (0 until 5).forEach { this[it] = it + 1 } }

    @Benchmark
    fun normalCall(): Constructor5 = topLevelFun5(1, 2, 3, 4, 5)

    @Benchmark
    fun kFunctionCall(): Constructor5 = function.call(1, 2, 3, 4, 5)

    @Benchmark
    fun kFunctionCallBy(): Constructor5 = function.callBy(argumentMap)

    @Benchmark
    fun javaMethod(): Constructor5 = javaMethod.invoke(null, 1, 2, 3, 4, 5) as Constructor5

    @Benchmark
    fun fastKFunctionCall(): Constructor5 = fastKFunction.call(1, 2, 3, 4, 5)

    @Benchmark
    fun fastKFunctionCallByCollection(): Constructor5 = fastKFunction.callByCollection(collection)

    @Benchmark
    fun fastKFunctionCallBy(): Constructor5 = fastKFunction.callBy(argumentBucket)
}
