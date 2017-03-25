package com.example.heshixiyang.mydiskcache;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.heshixiyang.mydiskcache.binaryResource.BinaryResource;
import com.example.heshixiyang.mydiskcache.cacheEventAndListenner.CacheEvent;
import com.example.heshixiyang.mydiskcache.cacheEventAndListenner.CacheEventListener;
import com.example.heshixiyang.mydiskcache.cacheKey.SimpleCacheKey;
import com.example.heshixiyang.mydiskcache.core.DefaultDiskStorage;
import com.example.heshixiyang.mydiskcache.core.DiskCacheConfig;
import com.example.heshixiyang.mydiskcache.core.DiskStorageCache;
import com.example.heshixiyang.mydiskcache.core.FileCache;
import com.example.heshixiyang.mydiskcache.core.WriterCallback;
import com.example.heshixiyang.mydiskcache.util.FileUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    FileCache mFileCache;
    Button buttonInsert;
    Button buttonHasKey;
    Button buttonRemove;
    Button buttonClearAll;
    Button buttonGetCache;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDiskCache();
        initView();

        final int[] times = {0};
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleCacheKey simpleCacheKey=new SimpleCacheKey(String.valueOf(times[0]));
                times[0]++;
                insert(simpleCacheKey);
            }
        });

        buttonHasKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "key 5 是否存在？" + mFileCache.hasKey(new SimpleCacheKey("5")), Toast.LENGTH_SHORT).show();
            }
        });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                times[0]--;
                SimpleCacheKey simpleCacheKey=new SimpleCacheKey(String.valueOf(times[0]));
                mFileCache.remove(simpleCacheKey);
            }
        });

        buttonClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFileCache.clearAll();
                times[0]=0;
            }
        });

        buttonGetCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCache();
            }
        });
    }

    private void initDiskCache(){

        DiskCacheConfig diskCacheConfig=DiskCacheConfig.newBuilder(this).build();
        Toast.makeText(this, "缓存文件夹："+diskCacheConfig.getBaseDirectoryPathSupplier().get().getPath(), Toast.LENGTH_SHORT).show();
        DefaultDiskStorage defaultDiskStorage=new DefaultDiskStorage(
                diskCacheConfig.getBaseDirectoryPathSupplier().get(),
                diskCacheConfig.getVersion(),
                diskCacheConfig.getCacheErrorLogger());

        DiskStorageCache.Params params = new DiskStorageCache.Params(
                diskCacheConfig.getMinimumSizeLimit(),
                diskCacheConfig.getLowDiskSpaceSizeLimit(),
                diskCacheConfig.getDefaultSizeLimit());


        CacheEventListener cacheEventListener=new CacheEventListener() {
            @Override
            public void onHit(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache hit", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onMiss(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache miss", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onWriteAttempt(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache write start", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onWriteSuccess(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache write success", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onReadException(CacheEvent cacheEvent) {
                Log.d("MainActivity Cache ReadException", cacheEvent.getCacheKey().getUriString());
            }

            @Override
            public void onWriteException(CacheEvent cacheEvent) {
                Log.d("MainActivity Cache WriteException", cacheEvent.getCacheKey().getUriString());
            }

            @Override
            public void onEviction(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache Eviction", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onCleared() throws IOException {
                Log.d("MainActivity", "Cleared");
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }
        };

        mFileCache=new DiskStorageCache(
                defaultDiskStorage,
                diskCacheConfig.getEntryEvictionComparatorSupplier(),
                params,
                cacheEventListener,
                diskCacheConfig.getCacheErrorLogger(),
                diskCacheConfig.getDiskTrimmableRegistry(),
                diskCacheConfig.getContext(),
                Executors.newSingleThreadExecutor(),
                diskCacheConfig.getIndexPopulateAtStartupEnabled());

    }

    private void initView(){
        buttonInsert=(Button)findViewById(R.id.insert);
        buttonHasKey=(Button)findViewById(R.id.hasKey);
        buttonRemove=(Button)findViewById(R.id.remove);
        buttonClearAll=(Button)findViewById(R.id.clearAll);
        buttonGetCache=(Button)findViewById(R.id.getCache);
        imageView=(ImageView)findViewById(R.id.image);
    }

    private void insert(SimpleCacheKey simpleCacheKey){
        try {
            mFileCache.insert(simpleCacheKey, new WriterCallback() {
                @Override
                public void write(OutputStream os) throws IOException {
                    FileUtils.bitmapToFile(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher),os);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCache(){
        BinaryResource diskCacheResource = mFileCache.getResource(new SimpleCacheKey("2"));
        if (diskCacheResource==null) Toast.makeText(this, "miss 2", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this, "hit 2", Toast.LENGTH_SHORT).show();
            try {
                imageView.setImageBitmap(BitmapFactory.decodeStream(diskCacheResource.openStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
