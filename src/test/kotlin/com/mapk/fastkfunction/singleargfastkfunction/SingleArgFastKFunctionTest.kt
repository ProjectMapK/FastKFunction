package com.mapk.fastkfunction.singleargfastkfunction

import com.mapk.fastkfunction.SingleArgFastKFunction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

private fun topLevelFunc(arg: Int): SingleArgSingleArgFastKFunctionCallTest.Dst = SingleArgSingleArgFastKFunctionCallTest.Dst(arg)
private fun SingleArgSingleArgFastKFunctionCallTest.Class.topLevelExtensionFunc(arg: Int): SingleArgSingleArgFastKFunctionCallTest.Dst =
    SingleArgSingleArgFastKFunctionCallTest.Dst(this.arg + arg)

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
private class SingleArgSingleArgFastKFunctionCallTest {
    class Class(val arg: Int)

    data class Dst(val arg: Int) {
        companion object {
            fun of(arg: Int) = Dst(arg)
        }
    }

    private fun instanceFunction(arg: Int) = Dst(arg)

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    fun call(target: KFunction<Dst>, instance: Any?, message: String) {
        val sut = SingleArgFastKFunction.of(target, instance)
        assertDoesNotThrow("Fail $message") {
            Assertions.assertEquals(Dst(100), sut.call(100), message)
        }
    }

    fun argumentsProvider(): Stream<Arguments> {
        val companionRawFunc = Dst::class.companionObject!!.functions.first { it.name == "of" }

        return listOf(
            Arguments.of(::Dst, null, "constructor"),
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
            Arguments.of(Class::topLevelExtensionFunc, Class(0), "top level extension func from class"),
            Class(0).let {
                Arguments.of(it::topLevelExtensionFunc, null, "top level extension func from instance")
            },
            Class(0).let {
                Arguments.of(it::topLevelExtensionFunc, it, "top level extension func from instance with instance")
            }
        ).stream()
    }
}
