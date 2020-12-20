package com.mapk.fastkfunction

import com.mapk.fastkfunction.SingleArgFastKFunction.Companion.checkParameters
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod

private fun topLevelFunc(arg: String) = println(arg)
private fun String.topLevelExtensionFunc(arg: String) = println(this + arg)

private class SingleArgFastKFunctionTest {
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CheckParametersTest {
        val valueParameter = mockk<KParameter> {
            every { kind } returns KParameter.Kind.VALUE
        }
        val instanceParameter = mockk<KParameter> {
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

        @ParameterizedTest
        @MethodSource("illegalSizeArgumentsProvider")
        fun isIllegalSizeArguments(params: List<KParameter>) {
            assertThrows<IllegalArgumentException> {
                params.checkParameters()
            }
        }

        fun illegalSizeArgumentsProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(listOf(valueParameter, valueParameter)),
            Arguments.of(listOf(instanceParameter, valueParameter, valueParameter))
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
                    SingleArgFastKFunction.topLevelFunctionOf(function, null, parameters, javaMethod)
                }
            }

            @Test
            fun isCorrect() {
                val result = assertDoesNotThrow {
                    SingleArgFastKFunction.topLevelFunctionOf(function, "", parameters, javaMethod)
                }
                assertTrue(result is SingleArgFastKFunction.TopLevelExtensionFunction)
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
                    SingleArgFastKFunction.topLevelFunctionOf(function, "", parameters, javaMethod)
                }
                assertTrue(result is SingleArgFastKFunction.TopLevelExtensionFunction)
            }

            @Test
            fun withoutInstanceTest() {
                val result = assertDoesNotThrow {
                    SingleArgFastKFunction.topLevelFunctionOf(function, null, parameters, javaMethod)
                }
                assertTrue(result is SingleArgFastKFunction.Function)
            }
        }

        @Test
        fun topLevelFunctionTest() {
            val function: KFunction<Unit> = ::topLevelFunc
            val parameters = function.parameters

            val result = assertDoesNotThrow {
                SingleArgFastKFunction.topLevelFunctionOf(function, null, parameters, function.javaMethod!!)
            }
            assertTrue(result is SingleArgFastKFunction.TopLevelFunction)
        }
    }

    @Nested
    inner class InstanceFunctionOfTest {
        fun instanceFunc(arg: String) = println(arg)

        @Nested
        inner class KindIsInstance {
            val function: KFunction<Unit> = InstanceFunctionOfTest::instanceFunc
            val parameters = function.parameters
            val javaMethod = function.javaMethod!!

            @Test
            fun withoutInstance() {
                assertThrows<IllegalArgumentException> {
                    SingleArgFastKFunction.instanceFunctionOf(function, null, parameters, javaMethod)
                }
            }

            @Test
            fun withWrongInstance() {
                assertThrows<IllegalArgumentException> {
                    SingleArgFastKFunction.instanceFunctionOf(function, "", parameters, javaMethod)
                }
            }

            @Test
            fun isCorrect() {
                val result = assertDoesNotThrow {
                    SingleArgFastKFunction.instanceFunctionOf(
                        function, this@InstanceFunctionOfTest, parameters, javaMethod
                    )
                }
                assertTrue(result is SingleArgFastKFunction.InstanceFunction)
            }
        }

        @Nested
        inner class InstanceFunction {
            val function: KFunction<Unit> = this@InstanceFunctionOfTest::instanceFunc
            val parameters = function.parameters
            val javaMethod = function.javaMethod!!

            @Test
            fun withoutInstance() {
                val result = assertDoesNotThrow {
                    SingleArgFastKFunction.instanceFunctionOf(
                        function, null, parameters, javaMethod
                    )
                }
                assertTrue(result is SingleArgFastKFunction.Function)
            }

            @Test
            fun withWrongInstance() {
                assertThrows<IllegalArgumentException> {
                    SingleArgFastKFunction.instanceFunctionOf(function, "", parameters, javaMethod)
                }
            }

            @Test
            fun withCorrectInstance() {
                val result = assertDoesNotThrow {
                    SingleArgFastKFunction.instanceFunctionOf(
                        function, this@InstanceFunctionOfTest, parameters, javaMethod
                    )
                }
                assertTrue(result is SingleArgFastKFunction.InstanceFunction)
            }
        }
    }
}
