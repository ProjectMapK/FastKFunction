package com.mapk.fastkfunction.fastkfunction

import com.mapk.fastkfunction.FastKFunction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

private fun topLevelFunc(arg1: Int, arg2: String): FullInitializedCallTest.Dst =
    FullInitializedCallTest.Dst(arg1, arg2)

private fun FullInitializedCallTest.Class.topLevelExtensionFunc(arg1: Int, arg2: String): FullInitializedCallTest.Dst =
    FullInitializedCallTest.Dst(arg1, arg2)

/**
 * 網羅しているパターン
 * - コンストラクタ
 * - インスタンスメソッド
 * - インスタンスメソッド + インスタンス
 * - コンパニオンオブジェクトに定義したメソッド
 * - コンパニオンオブジェクトに定義したメソッド + コンパニオンオブジェクトインスタンス
 * - リフレクションで取得したコンパニオンオブジェクトに定義したメソッド
 * - リフレクションで取得したコンパニオンオブジェクトに定義したメソッド + コンパニオンオブジェクトインスタンス
 * - トップレベル関数
 * - クラスから取得したトップレベル拡張関数 + レシーバインスタンス
 * - インスタンスから取得したトップレベル拡張関数
 * - インスタンスから取得したトップレベル拡張関数 + インスタンス
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
private class FullInitializedCallTest {
    class Class

    data class Dst(val arg1: Int, val arg2: String) {
        companion object {
            fun of(arg1: Int, arg2: String) = Dst(arg1, arg2)
        }
    }

    private fun instanceFunction(arg1: Int, arg2: String) = Dst(arg1, arg2)

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    fun callByArgumentBucket(target: KFunction<Dst>, instance: Any?, message: String) {
        val sut = FastKFunction.of(target, instance)
        val bucket = sut.generateBucket().apply {
            val params = target.parameters.filter { it.kind == KParameter.Kind.VALUE }

            set(params[0], 100)
            set(params[1], "txt")
        }

        assertDoesNotThrow("Fail $message") {
            assertEquals(Dst(100, "txt"), sut.callBy(bucket), message)
        }
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    fun callByCollection(target: KFunction<Dst>, instance: Any?, message: String) {
        val sut = FastKFunction.of(target, instance)
        assertDoesNotThrow("Fail $message") {
            assertEquals(Dst(100, "txt"), sut.callByCollection(listOf(100, "txt")), message)
        }
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    fun callByVarargs(target: KFunction<Dst>, instance: Any?, message: String) {
        val sut = FastKFunction.of(target, instance)
        assertDoesNotThrow("Fail $message") {
            assertEquals(Dst(100, "txt"), sut.call(100, "txt"), message)
        }
    }

    fun argumentsProvider(): Stream<Arguments> {
        val companionRawFunc = Dst::class.companionObject!!.functions.first { it.name == "of" }

        return listOf(
            Arguments.of(FullInitializedCallTest::Dst, null, "constructor"),
            Arguments.of(::instanceFunction, null, "instance func"),
            Arguments.of(::instanceFunction, this, "instance func with instance"),
            Arguments.of((Dst)::of, null, "companion object func"),
            Arguments.of((Dst)::of, Dst::class.companionObjectInstance, "companion object func with instance"),
            Arguments.of(companionRawFunc, null, "companion object func from reflection"),
            Arguments.of(
                companionRawFunc,
                Dst::class.companionObjectInstance,
                "companion object func from reflection with instance"
            ),
            Arguments.of(::topLevelFunc, null, "top level func"),
            Arguments.of(Class::topLevelExtensionFunc, Class(), "top level extension func from class"),
            Class().let {
                Arguments.of(it::topLevelExtensionFunc, null, "top level extension func from instance")
            },
            Class().let {
                Arguments.of(it::topLevelExtensionFunc, it, "top level extension func from instance with instance")
            }
        ).stream()
    }
}
