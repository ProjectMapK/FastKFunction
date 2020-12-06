package com.mapk.fastkfunction.singleargfastkfunction

data class Constructor1(val arg: Int) {
    fun instanceFun1(arg: Int) = Constructor1(this.arg + arg)

    companion object {
        fun companionObjectFun1(arg: Int) = Constructor1(arg)
    }
}

fun topLevelFun1(arg: Int) = Constructor1(arg)

fun Constructor1.topLevelExtensionFun1(arg: Int) = Constructor1(this.arg + arg)
