package com.example.heshixiyang.mydiskcache.comparator;

/**
 * Created by heshixiyang on 2017/3/23.
 */

import com.example.heshixiyang.mydiskcache.core.DiskStorage;

import java.util.Comparator;

/**
 * 定义一个比较器，比较哪个缓存应该被驱逐
 */
public interface EntryEvictionComparator extends Comparator<DiskStorage.Entry> {
}
