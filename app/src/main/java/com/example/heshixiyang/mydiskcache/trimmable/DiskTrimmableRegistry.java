package com.example.heshixiyang.mydiskcache.trimmable;

/**
 * Created by heshixiyang on 2017/3/23.
 */
/**
 * 一个让其他一系列class接收系统磁盘事件的类
 * 当需要削减磁盘使用量的时候，实现了这个接口的class应该通知所有已经注册过的trimmable。
 */
public interface DiskTrimmableRegistry {

    void registerDiskTrimmable(DiskTrimmable trimmable);

    void unregisterDiskTrimmable(DiskTrimmable trimmable);
}

