package com.mapk.fastkfunction.argumentBucket

import com.mapk.fastkfunction.argumentbucket.BucketGenerator
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

private fun String.extensionFunction(arg1: String, arg2: String): String = this + arg1 + arg2

class BucketGeneratorTest {
    private fun instanceFunction(arg1: String, arg2: String): String = arg1 + arg2

    companion object {
        fun companionObjectFunction(arg1: String, arg2: String): String = arg1 + arg2
    }

    @Test
    fun withoutInstanceTest() {
        val generator = BucketGenerator(::instanceFunction.parameters, null)

        assertArrayEquals(arrayOfNulls<Any?>(2), generator.getOriginalValueArray())
        assertArrayEquals(BooleanArray(2), generator.getOriginalInitializationStatuses())
        assertArrayEquals(Array(2) { it }, generator.getValueArrayGetter().invoke(Array(2) { it }))
    }

    @Nested
    inner class WithInstanceTest {
        @Test
        fun companionObjectFunction() {
            val companionObject = BucketGeneratorTest::class.companionObjectInstance!!
            val function = companionObject::class.functions.first { it.name == "companionObjectFunction" }

            val generator = BucketGenerator(function.parameters, companionObject)

            assertArrayEquals(arrayOf(companionObject, null, null), generator.getOriginalValueArray())
            assertArrayEquals(booleanArrayOf(true, false, false), generator.getOriginalInitializationStatuses())
            assertArrayEquals(arrayOf(1, 2), generator.getValueArrayGetter().invoke(Array(3) { it }))
        }

        @Test
        fun extensionFunction() {
            val function = String::extensionFunction

            val generator = BucketGenerator(function.parameters, "instance")

            assertArrayEquals(arrayOf("instance", null, null), generator.getOriginalValueArray())
            assertArrayEquals(booleanArrayOf(true, false, false), generator.getOriginalInitializationStatuses())
            assertArrayEquals(arrayOf(1, 2), generator.getValueArrayGetter().invoke(Array(3) { it }))
        }
    }
}
