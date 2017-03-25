package com.example.heshixiyang.mydiskcache.util;

/**
 * Created by heshixiyang on 2017/3/23.
 */
public class SystemClock implements Clock {

    private static final SystemClock INSTANCE = new SystemClock();

    private SystemClock() {
    }

    public static SystemClock get() {
        return INSTANCE;
    }

    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}

