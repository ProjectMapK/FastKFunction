package com.mapk.fastkfunction.argumentbucket

import org.jetbrains.annotations.TestOnly
import kotlin.reflect.KParameter

// 初期化前の値
internal val ABSENT_VALUE = Any()

internal class BucketGenerator(private val parameters: List<KParameter>, instance: Any?) {
    private val originalValueArray: Array<Any?> = Array(parameters.size) { null }
    private val originalInitializationStatuses: BooleanArray = BooleanArray(parameters.size)
    private val valueArrayGetter: (Array<Any?>) -> Array<Any?>

    init {
        if (parameters[0].kind != KParameter.Kind.VALUE) {
            originalInitializationStatuses[0] = true
            originalValueArray[0] = instance
            valueArrayGetter = { it.copyOfRange(1, parameters.size) }
        } else {
            valueArrayGetter = { it }
        }
    }

    @TestOnly
    fun getOriginalValueArray() = originalValueArray.clone()
    @TestOnly
    fun getOriginalInitializationStatuses() = originalInitializationStatuses.clone()
    @TestOnly
    fun getValueArrayGetter() = valueArrayGetter

    fun generateBucket(): ArgumentBucket =
        ArgumentBucket(parameters, originalValueArray.clone(), originalInitializationStatuses.clone(), valueArrayGetter)
}
