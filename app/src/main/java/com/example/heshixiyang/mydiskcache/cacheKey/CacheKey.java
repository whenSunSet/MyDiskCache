package com.example.heshixiyang.mydiskcache.cacheKey;

/**
 * Created by heshixiyang on 2017/3/23.
 */

import android.net.Uri;

/**
 * 一个强缓存键代替Object
 */
public interface CacheKey {

    /**
     * 这个对于调试有用处
     * */
    String toString();

    /**
     * 这个方法需要被实现，否则cache key会使用引用地址来判断相同
     * */
    boolean equals(Object o);

    /**
     * 这个方法需要被实现，其被用在{@link #equals}方法中
     * */
    int hashCode();

    /**
     * 返回true，如果这个key是使用{@link Uri}构建的
     */
    boolean containsUri(Uri uri);

    /**
     * 返回一个字符串表示的URI的核心缓存键。在包含多个键的情况下,返回第一个
     */
    String getUriString();
}
