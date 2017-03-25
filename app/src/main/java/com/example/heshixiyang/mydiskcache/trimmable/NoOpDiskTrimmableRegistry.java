package com.example.heshixiyang.mydiskcache.trimmable;

/**
 * Created by heshixiyang on 2017/3/23.
 */
/**
 * 实现了{@link DiskTrimmableRegistry}但是没有做任何事情，一个空的实现.
 */
public class NoOpDiskTrimmableRegistry implements DiskTrimmableRegistry {
    private static NoOpDiskTrimmableRegistry sInstance = null;

    private NoOpDiskTrimmableRegistry() {
    }

    public static synchronized NoOpDiskTrimmableRegistry getInstance() {
        if (sInstance == null) {
            sInstance = new NoOpDiskTrimmableRegistry();
        }
        return sInstance;
    }

    @Override
    public void registerDiskTrimmable(DiskTrimmable trimmable) {
    }

    @Override
    public void unregisterDiskTrimmable(DiskTrimmable trimmable) {
    }
}

