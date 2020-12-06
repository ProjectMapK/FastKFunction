package com.mapk.fastkfunction.fastkfunction

data class Constructor5(
    val arg1: Int,
    val arg2: Int,
    val arg3: Int,
    val arg4: Int,
    val arg5: Int
) {
    fun instanceFun5(arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int) = Constructor5(
        this.arg1 + arg1,
        this.arg2 + arg2,
        this.arg3 + arg3,
        this.arg4 + arg4,
        this.arg5 + arg5
    )

    companion object {
        fun companionObjectFun5(
            arg1: Int,
            arg2: Int,
            arg3: Int,
            arg4: Int,
            arg5: Int
        ) = Constructor5(arg1, arg2, arg3, arg4, arg5)
    }
}

fun topLevelFun5(
    arg1: Int,
    arg2: Int,
    arg3: Int,
    arg4: Int,
    arg5: Int
) = Constructor5(arg1, arg2, arg3, arg4, arg5)

fun Constructor5.topLevelExtensionFun5(arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int) = Constructor5(
    this.arg1 + arg1,
    this.arg2 + arg2,
    this.arg3 + arg3,
    this.arg4 + arg4,
    this.arg5 + arg5
)
