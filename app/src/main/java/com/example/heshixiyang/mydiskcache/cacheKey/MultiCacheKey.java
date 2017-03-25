package com.example.heshixiyang.mydiskcache.cacheKey;

/**
 * Created by heshixiyang on 2017/3/23.
 */

import android.net.Uri;

import com.example.heshixiyang.mydiskcache.util.Preconditions;

import java.util.List;

/**
 * 一个包装了多个cache key的 cache key
 */
public class MultiCacheKey implements CacheKey {

    final List<CacheKey> mCacheKeys;

    public MultiCacheKey(List<CacheKey> cacheKeys) {
        mCacheKeys = Preconditions.checkNotNull(cacheKeys);
    }

    public List<CacheKey> getCacheKeys() {
        return mCacheKeys;
    }

    @Override
    public String toString() {
        return "MultiCacheKey:" + mCacheKeys.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof MultiCacheKey) {
            final MultiCacheKey otherKey = (MultiCacheKey) o;
            return mCacheKeys.equals(otherKey.mCacheKeys);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mCacheKeys.hashCode();
    }

    @Override
    public boolean containsUri(Uri uri) {
        for (int i = 0; i < mCacheKeys.size(); i++) {
            if (mCacheKeys.get(i).containsUri(uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getUriString() {
        return mCacheKeys.get(0).getUriString();
    }
}
