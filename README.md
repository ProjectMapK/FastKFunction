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

val fastKFunction: FastKFunction<Sample> = FastKFunction(function)

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
TODO

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

val fastKFunction = FastKFunction(sample::instanceFun, sample)
```

Depending on how you get the `KFunction`, the `instance parameter` may be required.
Even if the `instance parameter` is not required, passing an `instance parameter` will make the call faster.
