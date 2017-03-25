package com.example.heshixiyang.mydiskcache.cacheEventAndListenner;

/**
 * Created by heshixiyang on 2017/3/23.
 */
/**
 * 不做任何事情的CacheEventListener，具体实现交给使用者
 * Implementation of {@link CacheEventListener} that doesn't do anything.
 */
public class NoOpCacheEventListener implements CacheEventListener {
    private static NoOpCacheEventListener sInstance = null;

    private NoOpCacheEventListener() {
    }

    public static synchronized NoOpCacheEventListener getInstance() {
        if (sInstance == null) {
            sInstance = new NoOpCacheEventListener();
        }
        return sInstance;
    }

    @Override
    public void onHit(CacheEvent cacheEvent) {

    }

    @Override
    public void onMiss(CacheEvent cacheEvent) {
    }

    @Override
    public void onWriteAttempt(CacheEvent cacheEvent) {
    }

    @Override
    public void onWriteSuccess(CacheEvent cacheEvent) {
    }

    @Override
    public void onReadException(CacheEvent cacheEvent) {
    }

    @Override
    public void onWriteException(CacheEvent cacheEvent) {
    }

    @Override
    public void onEviction(CacheEvent cacheEvent) {
    }

    @Override
    public void onCleared() {
    }
}

