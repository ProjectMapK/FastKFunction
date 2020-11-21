package com.mapk.fastkfunction.argumentbucket

import java.lang.UnsupportedOperationException
import kotlin.reflect.KParameter

class ArgumentBucket(
    private val keyList: List<KParameter>,
    private val valueArray: Array<Any?>,
    private val initializationStatuses: BooleanArray,
    private val valueArrayGetter: (Array<Any?>) -> Array<Any?>
) : Map<KParameter, Any?> {
    class Entry internal constructor(
        override val key: KParameter,
        override var value: Any?
    ) : Map.Entry<KParameter, Any?>

    fun isFullInitialized(): Boolean = initializationStatuses.all { it }

    fun getValueArray(): Array<Any?> = valueArrayGetter(valueArray)

    fun put(key: KParameter, value: Any?): Any? {
        return valueArray[key.index].apply {
            valueArray[key.index] = value
            initializationStatuses[key.index] = true
        }
    }

    fun put(index: Int, value: Any?): Any? {
        return valueArray[index].apply {
            valueArray[index] = value
            initializationStatuses[index] = true
        }
    }

    fun putIfAbsent(key: KParameter, value: Any?): Any? {
        return if (initializationStatuses[key.index])
            valueArray[key.index]
        else
            null.apply {
                valueArray[key.index] = value
                initializationStatuses[key.index] = true
            }
    }

    fun putIfAbsent(index: Int, value: Any?): Any? {
        return if (initializationStatuses[index])
            valueArray[index]
        else
            null.apply {
                valueArray[index] = value
                initializationStatuses[index] = true
            }
    }

    override val entries: Set<Map.Entry<KParameter, Any?>>
        get() = keys.fold(HashSet()) { acc, cur ->
            acc.apply { if (initializationStatuses[cur.index]) add(Entry(cur, valueArray[cur.index])) }
        }
    override val keys: Set<KParameter>
        get() = keys.fold(HashSet()) { acc, cur ->
            acc.apply { if (initializationStatuses[cur.index]) add(cur) }
        }
    override val size: Int
        get() = throw UnsupportedOperationException()
    override val values: Collection<Any?>
        get() = valueArray.filterIndexed { idx, _ -> initializationStatuses[idx] }

    // keyはインスタンスの一致と初期化状態を見る
    override fun containsKey(key: KParameter): Boolean =
        keyList[key.index] === key && initializationStatuses[key.index]

    override fun containsValue(value: Any?): Boolean = valueArray.withIndex()
        .any { initializationStatuses[it.index] && it.value == value }

    override fun get(key: KParameter): Any? = valueArray[key.index]

    override fun isEmpty(): Boolean = throw UnsupportedOperationException()
}
