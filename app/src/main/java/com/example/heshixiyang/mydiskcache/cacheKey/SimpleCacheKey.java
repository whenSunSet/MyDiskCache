package com.example.heshixiyang.mydiskcache.cacheKey;

/**
 * Created by heshixiyang on 2017/3/23.
 */

import android.net.Uri;

import com.example.heshixiyang.mydiskcache.util.Preconditions;

/**
 * {@link CacheKey}的实现者，这个只是简单的包装了{@link String}
 * 使用这个CacheKey需要构建一个唯一的string，并且唯一地标识了被缓存的资源
 */
public class SimpleCacheKey implements CacheKey {
    final String mKey;

    public SimpleCacheKey(final String key) {
        mKey = Preconditions.checkNotNull(key);
    }

    @Override
    public String toString() {
        return mKey;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof SimpleCacheKey) {
            final SimpleCacheKey otherKey = (SimpleCacheKey) o;
            return mKey.equals(otherKey.mKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mKey.hashCode();
    }

    @Override
    public boolean containsUri(Uri uri) {
        return mKey.contains(uri.toString());
    }

    @Override
    public String getUriString() {
        return mKey;
    }
}
