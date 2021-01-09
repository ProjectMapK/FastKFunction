package com.mapk.fastkfunction.argumentbucket

import java.util.AbstractMap.SimpleEntry as Entry
import kotlin.reflect.KParameter

class ArgumentBucket(
    private val keyList: List<KParameter>,
    private val valueArray: Array<Any?>,
    private val initializationStatuses: BooleanArray,
    private val valueArrayGetter: (Array<Any?>) -> Array<Any?>
) : Map<KParameter, Any?> {
    fun isFullInitialized(): Boolean = initializationStatuses.all { it }

    // getValueArrayは内部処理でしか利用しないためinternal化
    internal fun getValueArray(): Array<Any?> = valueArrayGetter(valueArray)

    operator fun set(key: KParameter, value: Any?): Any? {
        return valueArray[key.index].apply {
            valueArray[key.index] = value
            initializationStatuses[key.index] = true
        }
    }

    operator fun set(index: Int, value: Any?): Any? {
        return valueArray[index].apply {
            valueArray[index] = value
            initializationStatuses[index] = true
        }
    }

    /**
     * If the specified key is not already associated with a value associates it with the given value and returns
     * {@code null}, else returns the current value.
     */
    fun setIfAbsent(key: KParameter, value: Any?): Any? {
        return if (initializationStatuses[key.index])
            valueArray[key.index]
        else
            null.apply {
                valueArray[key.index] = value
                initializationStatuses[key.index] = true
            }
    }

    /**
     * If the specified key is not already associated with a value associates it with the given value and returns
     * {@code null}, else returns the current value.
     */
    fun setIfAbsent(index: Int, value: Any?): Any? {
        return if (initializationStatuses[index])
            valueArray[index]
        else
            null.apply {
                valueArray[index] = value
                initializationStatuses[index] = true
            }
    }

    override val entries: Set<Map.Entry<KParameter, Any?>>
        get() = keyList.fold(HashSet()) { acc, cur ->
            acc.apply { if (initializationStatuses[cur.index]) add(Entry(cur, valueArray[cur.index])) }
        }
    override val keys: Set<KParameter>
        get() = keyList.fold(HashSet()) { acc, cur ->
            acc.apply { if (initializationStatuses[cur.index]) add(cur) }
        }
    override val size: Int get() = initializationStatuses.count { it }
    override val values: Collection<Any?>
        get() = valueArray.filterIndexed { idx, _ -> initializationStatuses[idx] }

    // keyはインスタンスの一致と初期化状態を見る
    override fun containsKey(key: KParameter): Boolean =
        keyList[key.index] === key && initializationStatuses[key.index]

    override fun containsValue(value: Any?): Boolean = valueArray.withIndex()
        .any { initializationStatuses[it.index] && it.value == value }

    override fun get(key: KParameter): Any? = valueArray[key.index]
    operator fun get(index: Int): Any? = valueArray[index]

    override fun isEmpty(): Boolean = size == 0
}
