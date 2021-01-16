package com.mapk.fastkfunction.argumentbucket

import org.jetbrains.annotations.TestOnly
import kotlin.reflect.KParameter

// 初期化前の値
internal val ABSENT_VALUE = Any()

internal class BucketGenerator(private val parameters: List<KParameter>, instance: Any?) {
    private val originalValueArray: Array<Any?> = Array(parameters.size) { ABSENT_VALUE }
    private val originalCount: Int
    private val valueArrayGetter: (Array<Any?>) -> Array<Any?>

    init {
        if (parameters[0].kind != KParameter.Kind.VALUE) {
            originalCount = 1
            originalValueArray[0] = instance
            valueArrayGetter = { it.copyOfRange(1, parameters.size) }
        } else {
            originalCount = 0
            valueArrayGetter = { it }
        }
    }

    @TestOnly
    fun getOriginalValueArray() = originalValueArray.clone()
    @TestOnly
    fun getOriginalCount() = originalCount
    @TestOnly
    fun getValueArrayGetter() = valueArrayGetter

    fun generateBucket(): ArgumentBucket =
        ArgumentBucket(parameters, originalValueArray.clone(), originalCount, valueArrayGetter)
}
