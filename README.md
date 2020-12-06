[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Lint, Test, and upload Coveradge with Gradle.](https://github.com/ProjectMapK/FastKFunction/workflows/Lint,%20Test,%20and%20upload%20Coveradge%20with%20Gradle./badge.svg)
[![codecov](https://codecov.io/gh/ProjectMapK/FastKFunction/branch/master/graph/badge.svg?token=LcZTfSL7c8)](https://codecov.io/gh/ProjectMapK/FastKFunction)

---

FastKFunction
===
`FastKFunction` is a wrapper library for fast calls to `KFunction`.

## Demo code
With just this description, you can call the `KFunction` faster.

```kotlin
data class Sample(
    val arg1: Int,
    val arg2: Int,
    val arg3: Int,
    val arg4: Int,
    val arg5: Int
)

val function: KFunction<Sample> = ::Sample

val fastKFunction: FastKFunction<Sample> = FastKFunction.of(function)

// call by vararg
val result: Sample = fastKFunction.call(1, 2, 3, 4, 5)

// call by Collection
val result: Sample = fastKFunction.callByCollection(listOf(1, 2, 3, 4, 5))

// call by ArgumentBucket
val result: Sample = fastKFunction.generateBucket()
        .apply { (0 until 5).forEach { this[it] = it + 1 }}
        .let { fastKFunction.callBy(it) }
```

## Installation
`FastKFunction` is published on JitPack.
You can use this library on maven, gradle and any other build tools.
Please see here for the introduction method.

- [ProjectMapK / FastKFunction](https://jitpack.io/#ProjectMapK/FastKFunction) 

## How to use FastKFunction.

### Instance parameter.
If you call an instance function, you can expect a faster call with `instance parameter`.

```kotlin
data class Sample(
    val arg1: Int,
    val arg2: Int
) {
    fun instanceFun(arg3: Int): Int = arg1 + arg2 + arg3
}

val sample = Sample(1, 2)

val fastKFunction = FastKFunction.of(sample::instanceFun, sample)
```

Depending on how you get the `KFunction`, the `instance parameter` may be required.
Even if the `instance parameter` is not required, passing an `instance parameter` will make the call faster.

### How to call.
`FastKFunction` supports two major types of calls.

#### Call by vararg or Collection.
Calling with `vararg` or `Collection` is faster if you don't need to use the default arguments and
 can get them in the order in which they are defined.

```kotlin
val fastKFunction: FastKFunction<Sample> = FastKFunction.of(function)

// call by vararg
val result: Sample = fastKFunction.call(1, 2, 3, 4, 5)

// call by Collection
val result: Sample = fastKFunction.callByCollection(listOf(1, 2, 3, 4, 5))
```

#### Call by ArgumentBucket.
If the default argument is expected to be used, a call using `ArgumentBucket` is available.

`ArgumentBucket` has interfaces like `MutableMap<KParameter, Any?>`, which can be used, for example, as follows.

```kotlin
data class Sample(
    val arg1: Int,
    val arg2: Int = 0,
    val arg3: String? = null
)

val fastKFunction: FastKFunction<Sample> = FastKFunction.of(::Sample)

fun map(src: Map<String, Any?>): Sample {
    return fastKFunction.generateBucket()
        .apply { 
            fastKFunction.valueParameters.forEach {
                if (src.containsKey(it.name!!)) this[it] = src.getValue(it.name!!)
            }
        }.let { fastKFunction.callBy(it) }
}
```

## Benchmark
You can run the benchmark with the `./gradlew jmh`.  
Please note that it will take about 2 hours in total if executed with the default settings.

```bash
./gradlew jmh
```
