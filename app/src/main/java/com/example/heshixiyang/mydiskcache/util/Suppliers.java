package com.example.heshixiyang.mydiskcache.util;

/**
 * Created by heshixiyang on 2017/3/23.
 */


/**
 * 包装Supplier
 * Wrapper for creating a Supplier.
 */
public class Suppliers {
    /**
     * Returns a Supplier which always returns {@code instance}.
     *
     * @param instance the instance that should always be provided.
     */
    public static <T> Supplier<T> of(final T instance) {
        return new Supplier<T>() {
            @Override
            public T get() {
                return instance;
            }
        };
    }
}

