package com.mapk.fastkfunction.argumentbucket

import kotlin.reflect.KParameter

class ArgumentBucket(
    private val keyList: List<KParameter>,
    val valueArray: Array<Any?>,
    private val initializationStatuses: BooleanArray
) : Map<KParameter, Any?> {
    class Entry internal constructor(
        override val key: KParameter,
        override var value: Any?
    ) : Map.Entry<KParameter, Any?>

    private var _size: Int = initializationStatuses.count { it }

    fun isFullInitialized(): Boolean = initializationStatuses.all { it }

    fun put(key: KParameter, value: Any?): Any? {
        return valueArray[key.index].apply {
            valueArray[key.index] = value
            initializationStatuses[key.index] = true
            _size++
        }
    }

    fun put(index: Int, value: Any?): Any? {
        return valueArray[index].apply {
            valueArray[index] = value
            initializationStatuses[index] = true
            _size++
        }
    }

    fun putIfAbsent(key: KParameter, value: Any?): Any? {
        return if (initializationStatuses[key.index])
            valueArray[key.index]
        else
            null.apply {
                valueArray[key.index] = value
                initializationStatuses[key.index] = true
                _size++
            }
    }

    fun putIfAbsent(index: Int, value: Any?): Any? {
        return if (initializationStatuses[index])
            valueArray[index]
        else
            null.apply {
                valueArray[index] = value
                initializationStatuses[index] = true
                _size++
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
        get() = _size
    override val values: Collection<Any?>
        get() = valueArray.filterIndexed { idx, _ -> initializationStatuses[idx] }

    // keyはインスタンスの一致を見る
    override fun containsKey(key: KParameter): Boolean = keyList[key.index] === key

    override fun containsValue(value: Any?): Boolean = valueArray.any { it == value }

    override fun get(key: KParameter): Any? = valueArray[key.index]

    override fun isEmpty(): Boolean = _size == 0
}
