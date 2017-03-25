package com.example.heshixiyang.mydiskcache.comparator;

/**
 * Created by heshixiyang on 2017/3/23.
 */

import android.support.annotation.VisibleForTesting;

import com.example.heshixiyang.mydiskcache.core.DiskStorage;

/**
 * 清除缓存项基于它们的大小和时间戳，需要设置age和size的权重，最后比价总分的大小
 */
public class ScoreBasedEvictionComparatorSupplier implements EntryEvictionComparatorSupplier {

    private final float mAgeWeight;
    private final float mSizeWeight;

    public ScoreBasedEvictionComparatorSupplier(float ageWeight, float sizeWeight) {
        mAgeWeight = ageWeight;
        mSizeWeight = sizeWeight;
    }

    @Override
    public EntryEvictionComparator get() {
        return new EntryEvictionComparator() {

            long now = System.currentTimeMillis();

            /**
             * Return <0 if lhs should be evicted before rhs.
             */
            @Override
            public int compare(DiskStorage.Entry lhs, DiskStorage.Entry rhs) {
                float score1 = calculateScore(lhs, now);
                float score2 = calculateScore(rhs, now);
                return score1 < score2 ? 1 : ((score2 == score1) ? 0 : -1);
            }
        };
    }

    /**
     * Calculates an eviction score.
     *
     * Entries with a higher eviction score should be evicted first.
     */
    @VisibleForTesting
    float calculateScore(DiskStorage.Entry entry, long now) {
        long ageMs = now - entry.getTimestamp();
        long bytes = entry.getSize();
        return mAgeWeight * ageMs + mSizeWeight * bytes;
    }
}
