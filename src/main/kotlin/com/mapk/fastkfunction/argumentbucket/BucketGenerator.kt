package com.mapk.fastkfunction.argumentbucket

import kotlin.reflect.KParameter

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

    fun generateBucket(): ArgumentBucket =
        ArgumentBucket(parameters, originalValueArray.clone(), originalInitializationStatuses.clone(), valueArrayGetter)
}
