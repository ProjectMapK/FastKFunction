package com.mapk.fastkfunction.argumentbucket

import kotlin.reflect.KParameter

internal class BucketGenerator(private val parameters: List<KParameter>, instance: Any?) {
    private val originalValueArray: Array<Any?> = Array(parameters.size) { null }
    private val originalInitializationStatuses: BooleanArray = BooleanArray(parameters.size)

    init {
        if (instance != null) {
            originalValueArray[0] = instance
            originalInitializationStatuses[0] = true
        }
    }

    fun generateBucket(): ArgumentBucket =
        ArgumentBucket(parameters, originalValueArray.clone(), originalInitializationStatuses.clone())
}
