package com.mapk.fastkfunction

import com.mapk.fastkfunction.FastKFunction.Companion.checkParameters
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod

private fun topLevelFunc(arg: String) = println(arg)
private fun String.topLevelExtensionFunc(arg: String) = println(this + arg)

private class FastKFunctionTest {
    val valueParameter = mockk<KParameter>() {
        every { kind } returns KParameter.Kind.VALUE
    }
    val instanceParameter = mockk<KParameter>() {
        every { kind } returns KParameter.Kind.INSTANCE
    }
    val extensionReceiverParameter = mockk<KParameter>() {
        every { kind } returns KParameter.Kind.EXTENSION_RECEIVER
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CheckParametersTest {
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

    @Nested
    inner class TopLevelFunctionOfTest {
        @Nested
        inner class KindIsExtensionFunction {
            val function: KFunction<Unit> = String::topLevelExtensionFunc
            val parameters = function.parameters
            val javaMethod = function.javaMethod!!

            @Test
            fun nullInstanceTest() {
                assertThrows<IllegalArgumentException> {
                    FastKFunction.topLevelFunctionOf(function, null, parameters, javaMethod)
                }
            }

            @Test
            fun isCorrect() {
                val result = assertDoesNotThrow {
                    FastKFunction.topLevelFunctionOf(function, "", parameters, javaMethod)
                }
                assertTrue(result is FastKFunction.TopLevelExtensionFunction)
            }
        }

        @Nested
        inner class ExtensionFunction {
            val instance = ""
            val function: KFunction<Unit> = instance::topLevelExtensionFunc
            val parameters = function.parameters
            val javaMethod = function.javaMethod!!

            @Test
            fun withInstanceTest() {
                val result = assertDoesNotThrow {
                    FastKFunction.topLevelFunctionOf(function, "", parameters, javaMethod)
                }
                assertTrue(result is FastKFunction.TopLevelExtensionFunction)
            }

            @Test
            fun withoutInstanceTest() {
                val result = assertDoesNotThrow {
                    FastKFunction.topLevelFunctionOf(function, null, parameters, javaMethod)
                }
                assertTrue(result is FastKFunction.Function)
            }
        }

        @Test
        fun topLevelFunctionTest() {
            val function: KFunction<Unit> = ::topLevelFunc
            val parameters = function.parameters

            val result = assertDoesNotThrow {
                FastKFunction.topLevelFunctionOf(function, null, parameters, function.javaMethod!!)
            }
            assertTrue(result is FastKFunction.TopLevelFunction)
        }
    }
}
