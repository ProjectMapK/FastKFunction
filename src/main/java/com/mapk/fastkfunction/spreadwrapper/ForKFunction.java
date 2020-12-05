package com.mapk.fastkfunction.spreadwrapper;

import kotlin.reflect.KFunction;
import org.jetbrains.annotations.NotNull;

public class ForKFunction<T> {
    private final KFunction<T> kFunction;

    public ForKFunction(@NotNull KFunction<T> kFunction) {
        this.kFunction = kFunction;
    }

    public T call(Object[] args) {
        return kFunction.call(args);
    }
}
