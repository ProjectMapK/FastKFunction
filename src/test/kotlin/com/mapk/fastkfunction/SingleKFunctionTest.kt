package com.mapk.fastkfunction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SingleKFunctionTest {
    data class Constructor(val arg: Int)

    @Nested
    inner class ConstructorTest {
        @Test
        fun test() {
            val function = SingleArgFastKFunction(::Constructor, null)

            val result = function.call(100)
            assertEquals(100, result.arg)
        }
    }
}
