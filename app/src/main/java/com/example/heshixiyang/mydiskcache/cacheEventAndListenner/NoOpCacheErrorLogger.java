package com.example.heshixiyang.mydiskcache.cacheEventAndListenner;

/**
 * Created by heshixiyang on 2017/3/23.
 */

import android.support.annotation.Nullable;

/**
 * 不做任何事情的CacheErrorLogger，具体实现交给使用者
 * An implementation of {@link CacheErrorLogger} that doesn't do anything.
 */
public class NoOpCacheErrorLogger implements CacheErrorLogger {
    private static NoOpCacheErrorLogger sInstance = null;

    public NoOpCacheErrorLogger() {
    }

    public static synchronized NoOpCacheErrorLogger getInstance() {
        if (sInstance == null) {
            sInstance = new NoOpCacheErrorLogger();
        }
        return sInstance;
    }

    @Override
    public void logError(
            CacheErrorCategory category,
            Class<?> clazz,
            String message,
            @Nullable Throwable throwable) {
    }
}

