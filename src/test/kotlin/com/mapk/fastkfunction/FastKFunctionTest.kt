package com.mapk.fastkfunction

import com.mapk.fastkfunction.FastKFunction.Companion.checkParameters
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KParameter

private class FastKFunctionTest {
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CheckParametersTest {
        val valueParameter = mockk<KParameter>() {
            every { kind } returns KParameter.Kind.VALUE
        }
        val instanceParameter = mockk<KParameter>() {
            every { kind } returns KParameter.Kind.INSTANCE
        }

        @ParameterizedTest
        @MethodSource("emptyParamListProvider")
        fun isEmpty(params: List<KParameter>) {
            assertThrows<IllegalArgumentException> { params.checkParameters() }
        }

        fun emptyParamListProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(emptyList<KParameter>()),
            Arguments.of(listOf(instanceParameter))
        )

        @Test
        fun isIllegalArguments() {
            assertThrows<IllegalArgumentException> {
                listOf(instanceParameter, instanceParameter, valueParameter).checkParameters()
            }
        }

        @ParameterizedTest
        @MethodSource("collectParamListProvider")
        fun isCorrect(params: List<KParameter>) {
            val actual = assertDoesNotThrow { params.checkParameters() }
            assertEquals(params, actual)
        }

        fun collectParamListProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(listOf(valueParameter)),
            Arguments.of(listOf(instanceParameter, valueParameter))
        )
    }
}
