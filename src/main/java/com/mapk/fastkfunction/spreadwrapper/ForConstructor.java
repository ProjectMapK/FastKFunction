package com.mapk.fastkfunction.spreadwrapper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ForConstructor<T> implements SpreadWrapper<T> {
    private final Constructor<T> constructor;

    public ForConstructor(@NotNull Constructor<T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public T call(Object[] args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return constructor.newInstance(args);
    }
}
