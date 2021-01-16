package com.mapk.fastkfunction.argumentbucket

import kotlin.reflect.KParameter
import java.util.AbstractMap.SimpleEntry as Entry

class ArgumentBucket(
    private val keyList: List<KParameter>,
    private val valueArray: Array<Any?>,
    private var count: Int,
    private val valueArrayGetter: (Array<Any?>) -> Array<Any?>
) : Map<KParameter, Any?> {
    fun isFullInitialized(): Boolean = count == keyList.size

    // getValueArrayは内部処理でしか利用しないためinternal化
    internal fun getValueArray(): Array<Any?> = valueArrayGetter(valueArray)

    operator fun set(key: KParameter, value: Any?): Any? = set(key.index, value)

    operator fun set(index: Int, value: Any?): Any? = if (isInitialized(index))
        valueArray[index].apply { valueArray[index] = value }
    else {
        count++
        valueArray[index] = value
        null // 値がABSENT_VALUEならnullを返す
    }

    /**
     * If the specified key is not already associated with a value associates it with the given value and returns
     * {@code null}, else returns the current value.
     */
    fun setIfAbsent(key: KParameter, value: Any?): Any? = setIfAbsent(key.index, value)

    /**
     * If the specified key is not already associated with a value associates it with the given value and returns
     * {@code null}, else returns the current value.
     */
    fun setIfAbsent(index: Int, value: Any?): Any? = if (isInitialized(index))
        valueArray[index]
    else {
        count++
        valueArray[index] = value
        null // 値がABSENT_VALUEならnullを返す
    }

    private fun isInitialized(index: Int): Boolean = valueArray[index] !== ABSENT_VALUE

    override val entries: Set<Map.Entry<KParameter, Any?>>
        get() = keyList.fold(HashSet()) { acc, cur ->
            acc.apply { if (isInitialized(cur.index)) add(Entry(cur, valueArray[cur.index])) }
        }
    override val keys: Set<KParameter>
        get() = keyList.fold(HashSet()) { acc, cur ->
            acc.apply { if (isInitialized(cur.index)) add(cur) }
        }
    override val size: Int get() = count
    override val values: Collection<Any?>
        get() = valueArray.filter { it !== ABSENT_VALUE }

    // keyはインスタンスの一致と初期化状態を見る
    override fun containsKey(key: KParameter): Boolean =
        keyList[key.index] === key && isInitialized(key.index)

    override fun containsValue(value: Any?): Boolean = valueArray.withIndex()
        .any { isInitialized(it.index) && it.value == value }

    override fun get(key: KParameter): Any? = valueArray[key.index]
    operator fun get(index: Int): Any? = valueArray[index]

    override fun isEmpty(): Boolean = count == 0
}
