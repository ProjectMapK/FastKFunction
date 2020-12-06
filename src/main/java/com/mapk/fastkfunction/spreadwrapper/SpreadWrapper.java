package com.mapk.fastkfunction.spreadwrapper;

import java.lang.reflect.InvocationTargetException;

/**
 * Wrapper to avoid using Kotlin's heavy spread operator.
 * @param <T> return type
 */
public interface SpreadWrapper<T> {
    T call(Object[] args) throws InvocationTargetException, IllegalAccessException, InstantiationException;
}
